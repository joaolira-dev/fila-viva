import { useEffect, useMemo, useState } from 'react'
import './App.css'

type Activity = { id: number; name: string; description: string; durationMinutes: number }
type Slot = { id: number; activityId: number; startsAt: string; available: boolean; waitingCount: number }
type Booking = { code: string; slotId: number; guestName: string; status: 'CONFIRMED' | 'WAITLISTED' | 'CANCELLED'; startsAt: string }

const API = import.meta.env.VITE_API_URL ?? ''
const zone = 'America/Sao_Paulo'
const dateKey = (value: string) => new Intl.DateTimeFormat('en-CA', { timeZone: zone, year: 'numeric', month: '2-digit', day: '2-digit' }).format(new Date(value))
const dayLabel = (value: string) => new Intl.DateTimeFormat('pt-BR', { timeZone: zone, weekday: 'short', day: '2-digit', month: 'short' }).format(new Date(value)).replaceAll('.', '')
const fullDate = (value: string) => new Intl.DateTimeFormat('pt-BR', { timeZone: zone, day: 'numeric', month: 'long', year: 'numeric' }).format(new Date(value))
const timeLabel = (value: string) => new Intl.DateTimeFormat('pt-BR', { timeZone: zone, hour: '2-digit', minute: '2-digit' }).format(new Date(value))

async function request<T>(path: string, init?: RequestInit): Promise<T> {
  const response = await fetch(`${API}${path}`, { ...init, headers: { 'Content-Type': 'application/json', ...init?.headers } })
  if (!response.ok) throw new Error(response.status === 404 ? 'Reserva não encontrada.' : `Não foi possível concluir a solicitação (HTTP ${response.status}).`)
  return response.json() as Promise<T>
}

function App() {
  const [activities, setActivities] = useState<Activity[]>([])
  const [slots, setSlots] = useState<Slot[]>([])
  const [activityId, setActivityId] = useState<number | null>(null)
  const [day, setDay] = useState('')
  const [slotId, setSlotId] = useState<number | null>(null)
  const [guestName, setGuestName] = useState('')
  const [booking, setBooking] = useState<Booking | null>(null)
  const [loading, setLoading] = useState(true)
  const [sending, setSending] = useState(false)
  const [error, setError] = useState('')

  async function refresh() {
    const [nextActivities, nextSlots] = await Promise.all([request<Activity[]>('/api/activities'), request<Slot[]>('/api/slots')])
    setActivities(nextActivities)
    setSlots(nextSlots)
    setActivityId(current => current ?? nextActivities[0]?.id ?? null)
    setDay(current => current || (nextSlots[0] ? dateKey(nextSlots[0].startsAt) : ''))
  }

  useEffect(() => {
    let active = true
    Promise.all([request<Activity[]>('/api/activities'), request<Slot[]>('/api/slots')])
      .then(async ([nextActivities, nextSlots]) => {
        if (!active) return
        setActivities(nextActivities)
        setSlots(nextSlots)
        setActivityId(nextActivities[0]?.id ?? null)
        setDay(nextSlots[0] ? dateKey(nextSlots[0].startsAt) : '')
        const code = localStorage.getItem('fila-viva-booking')
        if (code) {
          try {
            const saved = await request<Booking>(`/api/bookings/${code}`)
            if (active && saved.status !== 'CANCELLED') setBooking(saved)
          } catch { localStorage.removeItem('fila-viva-booking') }
        }
      })
      .catch(() => { if (active) setError('A agenda está indisponível. Confira se a API está em execução.') })
      .finally(() => { if (active) setLoading(false) })
    return () => { active = false }
  }, [])

  const activity = activities.find(item => item.id === activityId)
  const activitySlots = useMemo(() => slots.filter(item => item.activityId === activityId), [slots, activityId])
  const days = [...new Set(activitySlots.map(item => dateKey(item.startsAt)))]
  const visibleSlots = activitySlots.filter(item => dateKey(item.startsAt) === day)
  const selectedSlot = slots.find(item => item.id === slotId)
  const openCount = activitySlots.filter(item => item.available).length

  async function createBooking(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault()
    if (!slotId || !guestName.trim()) return
    setSending(true); setError('')
    try {
      const created = await request<Booking>('/api/bookings', { method: 'POST', body: JSON.stringify({ slotId, guestName: guestName.trim() }) })
      setBooking(created)
      localStorage.setItem('fila-viva-booking', created.code)
      setSlotId(null)
      await refresh()
    } catch (cause) { setError((cause as Error).message) }
    finally { setSending(false) }
  }

  async function cancelBooking() {
    if (!booking) return
    setSending(true); setError('')
    try {
      await request<Booking>(`/api/bookings/${booking.code}`, { method: 'DELETE' })
      setBooking(null)
      localStorage.removeItem('fila-viva-booking')
      await refresh()
    } catch (cause) { setError((cause as Error).message) }
    finally { setSending(false) }
  }

  async function checkBooking() {
    if (!booking) return
    setSending(true); setError('')
    try {
      setBooking(await request<Booking>(`/api/bookings/${booking.code}`))
      await refresh()
    } catch (cause) { setError((cause as Error).message) }
    finally { setSending(false) }
  }

  return <div className="app-shell">
    <header className="site-header">
      <a className="brand" href="#inicio" aria-label="Fila Viva, voltar ao início"><span className="brand-mark" aria-hidden="true"><i/><i/><i/></span>fila<span>viva</span><b>.</b></a>
      <nav aria-label="Navegação principal"><a href="#agenda">Agenda</a><a href="#como-funciona">Como funciona</a></nav>
      <span className="demo-badge"><span className="live-dot"/> Ambiente demonstrativo</span>
    </header>

    <main id="inicio">
      <section className="hero-section">
        <div className="hero-copy"><span className="eyebrow"><i/> SEU TEMPO TEM VALOR</span><h1>Um lugar na agenda.<br/><em>Sem ficar na espera.</em></h1><p>Escolha seu horário em poucos passos. Se ele já estiver ocupado, entre na fila e acompanhe sua reserva com transparência.</p><a className="hero-button" href="#agenda">Explorar horários <span>↗</span></a><div className="hero-footnote"><span className="mini-avatars"><b>✓</b><b>↗</b><b>✦</b></span>Agendamento simples, do início ao fim.</div></div>
        <div className="hero-art" aria-hidden="true"><div className="art-orbit"/><div className="art-orbit inner"/><div className="art-card"><div className="art-card-top">AGENDA DA SEMANA <span>↗</span></div><div className="art-days"><span>S<br/>19</span><span>T<br/>20</span><strong>Q<br/>21</strong><span>Q<br/>22</span><span>S<br/>23</span></div><div className="art-event"><span>✦</span><div><strong>Horário confirmado</strong><small>Hoje, 14:00</small></div><b>✓</b></div></div><div className="floating-note"><span>↗</span><div><strong>Sua vez chegou!</strong><small>Um horário ficou disponível.</small></div></div><span className="art-star">✳</span></div>
      </section>

      <section className="agenda-section" id="agenda"><div className="section-heading"><div><span className="eyebrow"><i/> SUA AGENDA</span><h2>Encontre o momento certo.</h2><p>Escolha um tipo de atendimento e veja os próximos horários.</p></div><span className="section-count">{loading ? 'Carregando agenda...' : `${openCount} horários livres`}</span></div>
        <div className="booking-layout"><div className="agenda-card">
          <div className="step-heading"><span>01</span><div><h3>O que você precisa?</h3><p>Selecione o tipo de atendimento.</p></div></div>
          <div className="activity-grid" role="group" aria-label="Tipos de atendimento">{activities.map((item, index) => <button key={item.id} type="button" className={`activity-option ${activityId === item.id ? 'selected' : ''}`} onClick={() => { setActivityId(item.id); setSlotId(null) }} aria-pressed={activityId === item.id}><span className="activity-icon">{index === 0 ? '✳' : '▣'}</span><strong>{item.name}</strong><small>{item.description}</small><span className="duration">◷ {item.durationMinutes} min</span></button>)}</div>
          <div className="divider"/>
          <div className="step-heading"><span>02</span><div><h3>Quando fica melhor?</h3><p>Todos os horários no fuso de Brasília.</p></div></div>
          <div className="day-list" role="group" aria-label="Escolha o dia">{days.map(date => <button type="button" key={date} className={day === date ? 'active' : ''} aria-pressed={day === date} onClick={() => { setDay(date); setSlotId(null) }}>{dayLabel(activitySlots.find(item => dateKey(item.startsAt) === date)!.startsAt)}</button>)}</div>
          <div className="slots-title"><strong>Horários</strong><span>{activity?.durationMinutes ?? 0} min por sessão</span></div>
          <div className="slot-grid">{visibleSlots.map(item => <button type="button" key={item.id} className={`${slotId === item.id ? 'active' : ''} ${!item.available ? 'waitlist' : ''}`} aria-pressed={slotId === item.id} onClick={() => setSlotId(item.id)}><strong>{timeLabel(item.startsAt)}</strong><small>{item.available ? 'Disponível' : `Lista de espera${item.waitingCount ? ` · ${item.waitingCount}` : ''}`}</small></button>)}</div>
          {!loading && visibleSlots.length === 0 && <p className="empty-note">Ainda não há horários para este dia.</p>}
        </div>
        <aside className="summary-card" aria-label="Resumo do agendamento"><div className="summary-top">✳ &nbsp; SEU AGENDAMENTO</div>{booking ? <div className="summary-body"><span className={`status-pill ${booking.status.toLowerCase()}`}>{booking.status === 'CONFIRMED' ? '✓ Confirmado' : '◷ Na lista de espera'}</span><h3>{booking.status === 'CONFIRMED' ? 'Seu horário está reservado.' : 'Você está na fila.'}</h3><p>{booking.status === 'CONFIRMED' ? 'Guarde o código para consultar ou cancelar.' : 'Quando a vaga for liberada, sua reserva será promovida automaticamente.'}</p><div className="summary-detail"><span>Data e horário</span><strong>{fullDate(booking.startsAt)} · {timeLabel(booking.startsAt)}</strong></div><div className="booking-code"><small>SEU CÓDIGO</small><code>{booking.code}</code></div><button type="button" className="primary-action" onClick={checkBooking} disabled={sending}>Atualizar status ↗</button><button type="button" className="text-action" onClick={cancelBooking} disabled={sending}>Cancelar agendamento</button></div> : <form className="summary-body" onSubmit={createBooking}><h3>Quase lá.</h3><p>Selecione um horário para ver os detalhes e concluir.</p><div className="summary-detail"><span>Atendimento</span><strong>{activity?.name ?? 'Escolha uma atividade'}</strong></div><div className="summary-detail"><span>Data e horário</span><strong>{selectedSlot ? `${fullDate(selectedSlot.startsAt)} · ${timeLabel(selectedSlot.startsAt)}` : 'Escolha um horário'}</strong></div>{selectedSlot && <><label htmlFor="guest-name">Seu nome</label><input id="guest-name" type="text" maxLength={80} required value={guestName} onChange={event => setGuestName(event.target.value)} placeholder="Como podemos chamar você?" autoComplete="name"/><p className="privacy-note">Use um apelido se preferir. Não pedimos e-mail nesta demonstração.</p></>}<button type="submit" className="primary-action" disabled={!selectedSlot || sending}>{sending ? 'Aguarde...' : !selectedSlot ? 'Escolha um horário ↗' : selectedSlot.available ? 'Confirmar horário ↗' : 'Entrar na lista ↗'}</button><div className="reassurance">✓ &nbsp; Sem cadastro. Sem complicação.</div></form>}{error && <p className="error-message" role="alert">{error}</p>}</aside></div>
      </section>

      <section className="how-section" id="como-funciona"><div><span className="eyebrow"><i/> TUDO NO SEU RITMO</span><h2>Simples como<br/><em>deveria ser.</em></h2></div><div className="how-steps"><div><span>01 / ESCOLHA</span><h3>Encontre um horário</h3><p>Explore a agenda e selecione o momento que funciona para você.</p></div><div><span>02 / RESERVE</span><h3>Confirme em segundos</h3><p>Informe seu nome e guarde o código da reserva.</p></div><div><span>03 / ACOMPANHE</span><h3>Conte com a fila viva</h3><p>Se o horário estiver ocupado, entre na lista e acompanhe quando a vaga for sua.</p></div></div></section>
    </main>
    <footer><a className="brand" href="#inicio">fila<span>viva</span><b>.</b></a><span>Uma demonstração de agendamento inteligente.</span><a href="#inicio">Voltar ao topo ↑</a></footer>
  </div>
}

export default App
