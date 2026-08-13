import { useEffect, useMemo, useState } from 'react'

const API_URL = import.meta.env.VITE_API_URL ?? 'http://localhost:8080/api/v1'
const STORAGE_KEY = 'assinaturas.subscription-id'

const PLAN_DETAILS = {
  BASICO: { name: 'Básico', price: '19,90', description: 'Para começar com o essencial.', accent: 'bg-lime-300' },
  PREMIUM: { name: 'Premium', price: '39,90', description: 'A experiência completa para você.', accent: 'bg-emerald-700' },
  FAMILIA: { name: 'Família', price: '59,90', description: 'Mais pessoas no mesmo plano.', accent: 'bg-amber-300' },
}

async function request(path, options = {}) {
  const response = await fetch(`${API_URL}${path}`, {
    headers: { 'Content-Type': 'application/json', ...options.headers },
    ...options,
  })

  if (response.ok) return response.json()

  const error = await response.json().catch(() => null)
  throw new Error(error?.message ?? 'Não foi possível concluir esta ação.')
}

function formatDate(value) {
  return new Intl.DateTimeFormat('pt-BR', { dateStyle: 'long' }).format(new Date(`${value}T12:00:00`))
}

function App() {
  const [plans, setPlans] = useState(Object.keys(PLAN_DETAILS))
  const [subscription, setSubscription] = useState(null)
  const [formOpen, setFormOpen] = useState(false)
  const [selectedPlan, setSelectedPlan] = useState('PREMIUM')
  const [form, setForm] = useState({ name: '', email: '' })
  const [notice, setNotice] = useState(null)
  const [busy, setBusy] = useState(false)

  useEffect(() => {
    request('/plans')
      .then((data) => setPlans(data))
      .catch(() => setNotice({ type: 'warning', message: 'API indisponível. Inicie o backend para usar o fluxo completo.' }))

    const subscriptionId = localStorage.getItem(STORAGE_KEY)
    if (!subscriptionId) return

    request(`/subscriptions/${subscriptionId}`)
      .then(setSubscription)
      .catch(() => localStorage.removeItem(STORAGE_KEY))
  }, [])

  const currentPlan = useMemo(
    () => subscription ? PLAN_DETAILS[subscription.plan] : null,
    [subscription],
  )

  function openSubscription(plan) {
    setSelectedPlan(plan)
    setFormOpen(true)
    setNotice(null)
  }

  async function createSubscription(event) {
    event.preventDefault()
    setBusy(true)
    setNotice(null)

    try {
      const user = await request('/users', {
        method: 'POST',
        body: JSON.stringify(form),
      })
      const createdSubscription = await request('/subscriptions', {
        method: 'POST',
        body: JSON.stringify({ userId: user.id, plan: selectedPlan }),
      })
      localStorage.setItem(STORAGE_KEY, createdSubscription.id)
      setSubscription(createdSubscription)
      setFormOpen(false)
      setForm({ name: '', email: '' })
      setNotice({ type: 'success', message: 'Assinatura criada com sucesso.' })
    } catch (error) {
      setNotice({ type: 'error', message: error.message })
    } finally {
      setBusy(false)
    }
  }

  async function cancelSubscription() {
    if (!subscription || !window.confirm('Deseja cancelar a renovação desta assinatura?')) return
    setBusy(true)
    setNotice(null)
    try {
      const canceled = await request(`/subscriptions/${subscription.id}/cancel`, { method: 'POST' })
      setSubscription(canceled)
      setNotice({ type: 'success', message: 'Renovação cancelada. O acesso continua até o fim do ciclo.' })
    } catch (error) {
      setNotice({ type: 'error', message: error.message })
    } finally {
      setBusy(false)
    }
  }

  async function openCheckout() {
    if (!subscription) return
    setBusy(true)
    setNotice(null)
    try {
      const checkout = await request(`/subscriptions/${subscription.id}/checkout`, { method: 'POST' })
      if (!checkout.checkoutUrl) throw new Error('Checkout temporariamente indisponível.')
      window.open(checkout.checkoutUrl, '_blank', 'noopener,noreferrer')
      setNotice({ type: 'success', message: 'Checkout aberto em uma nova aba.' })
    } catch (error) {
      setNotice({ type: 'error', message: error.message })
    } finally {
      setBusy(false)
    }
  }

  return (
    <main className="min-h-screen bg-stone-50">
      <header className="mx-auto flex max-w-6xl items-center justify-between px-6 py-7">
        <div className="flex items-center gap-3">
          <span className="grid size-10 place-items-center rounded-2xl bg-emerald-800 text-xl font-black text-lime-300">A</span>
          <div>
            <p className="text-lg font-black tracking-tight text-stone-900">assina</p>
            <p className="text-xs font-medium text-stone-500">gestão simples de assinaturas</p>
          </div>
        </div>
        <span className="rounded-full border border-emerald-900/10 bg-white px-3 py-1.5 text-xs font-semibold text-emerald-800">API Spring Boot</span>
      </header>

      <section className="mx-auto max-w-6xl px-6 pb-16">
        <div className="overflow-hidden rounded-[2rem] bg-emerald-900 px-8 py-10 text-white shadow-xl shadow-emerald-950/15 md:px-12 md:py-14">
          <p className="mb-4 text-sm font-bold uppercase tracking-[0.18em] text-lime-300">Seu plano, no seu ritmo</p>
          <div className="grid gap-8 md:grid-cols-[1.25fr_0.75fr] md:items-end">
            <div>
              <h1 className="max-w-xl text-4xl font-black tracking-tight md:text-5xl">Assinaturas que funcionam sem complicação.</h1>
              <p className="mt-4 max-w-xl text-base leading-7 text-emerald-100">Escolha um plano, acompanhe seu ciclo e cancele quando quiser. O acesso segue ativo até o vencimento.</p>
            </div>
            <div className="rounded-3xl bg-white/10 p-5 backdrop-blur">
              <p className="text-sm text-emerald-100">Renovação automática</p>
              <p className="mt-1 text-2xl font-black">3 tentativas seguras</p>
              <p className="mt-2 text-sm leading-5 text-emerald-100">Idempotência e controle de concorrência no backend.</p>
            </div>
          </div>
        </div>

        {notice && (
          <div className={`mt-6 rounded-2xl border px-5 py-4 text-sm font-medium ${notice.type === 'error' ? 'border-red-200 bg-red-50 text-red-800' : notice.type === 'warning' ? 'border-amber-200 bg-amber-50 text-amber-900' : 'border-emerald-200 bg-emerald-50 text-emerald-800'}`}>
            {notice.message}
          </div>
        )}

        <section className="mt-10 grid gap-6 lg:grid-cols-[0.9fr_1.1fr]">
          <div className="rounded-[2rem] border border-stone-200 bg-white p-7 shadow-sm">
            <p className="text-sm font-bold uppercase tracking-[0.16em] text-stone-500">Minha assinatura</p>
            {subscription ? (
              <>
                <div className="mt-6 flex items-start justify-between gap-4">
                  <div>
                    <h2 className="text-3xl font-black text-stone-900">{currentPlan?.name}</h2>
                    <p className="mt-1 text-stone-500">R$ {currentPlan?.price}/mês</p>
                  </div>
                  <span className={`rounded-full px-3 py-1 text-xs font-bold ${subscription.status === 'ACTIVE' ? 'bg-emerald-100 text-emerald-800' : 'bg-amber-100 text-amber-900'}`}>
                    {subscription.status === 'ACTIVE' ? 'ATIVA' : 'CANCELADA'}
                  </span>
                </div>
                <div className="mt-8 rounded-2xl bg-stone-50 p-4 text-sm">
                  <p className="text-stone-500">Próximo vencimento</p>
                  <p className="mt-1 font-bold text-stone-900">{formatDate(subscription.expirationDate)}</p>
                </div>
                <div className="mt-5 flex flex-wrap gap-3">
                  <button disabled={busy} onClick={openCheckout} className="rounded-xl bg-emerald-800 px-4 py-2.5 text-sm font-bold text-white transition hover:bg-emerald-700 disabled:opacity-50">Ir para checkout</button>
                  {subscription.status === 'ACTIVE' && <button disabled={busy} onClick={cancelSubscription} className="rounded-xl border border-stone-300 px-4 py-2.5 text-sm font-bold text-stone-700 transition hover:bg-stone-50 disabled:opacity-50">Cancelar renovação</button>}
                </div>
              </>
            ) : (
              <div className="mt-6 rounded-2xl bg-stone-50 p-5">
                <p className="font-bold text-stone-900">Nenhuma assinatura ativa.</p>
                <p className="mt-2 text-sm leading-6 text-stone-500">Escolha um plano ao lado para começar a demonstração.</p>
              </div>
            )}
          </div>

          <div>
            <p className="mb-4 text-sm font-bold uppercase tracking-[0.16em] text-stone-500">Planos disponíveis</p>
            <div className="grid gap-4 sm:grid-cols-3">
              {plans.map((plan) => {
                const detail = PLAN_DETAILS[plan]
                if (!detail) return null
                return (
                  <article key={plan} className="flex min-h-72 flex-col rounded-[2rem] border border-stone-200 bg-white p-6 shadow-sm transition hover:-translate-y-1 hover:shadow-lg">
                    <span className={`mb-7 size-3 rounded-full ${detail.accent}`} />
                    <h2 className="text-xl font-black text-stone-900">{detail.name}</h2>
                    <p className="mt-2 min-h-12 text-sm leading-6 text-stone-500">{detail.description}</p>
                    <p className="mt-6 text-2xl font-black text-stone-900">R$ {detail.price}<span className="text-sm font-medium text-stone-500">/mês</span></p>
                    <button onClick={() => openSubscription(plan)} className="mt-auto rounded-xl bg-stone-900 px-3 py-2.5 text-sm font-bold text-white transition hover:bg-stone-700">Escolher plano</button>
                  </article>
                )
              })}
            </div>
          </div>
        </section>
      </section>

      {formOpen && (
        <div className="fixed inset-0 z-10 grid place-items-center bg-stone-950/40 px-5 backdrop-blur-sm">
          <form onSubmit={createSubscription} className="w-full max-w-md rounded-[2rem] bg-white p-7 shadow-2xl">
            <div className="flex items-start justify-between gap-4">
              <div>
                <p className="text-sm font-bold uppercase tracking-[0.15em] text-emerald-700">Nova assinatura</p>
                <h2 className="mt-1 text-2xl font-black">Plano {PLAN_DETAILS[selectedPlan].name}</h2>
              </div>
              <button type="button" onClick={() => setFormOpen(false)} className="rounded-xl px-2 py-1 text-xl text-stone-400 hover:bg-stone-100">×</button>
            </div>
            <label className="mt-7 block text-sm font-bold text-stone-700">Nome
              <input required value={form.name} onChange={(event) => setForm({ ...form, name: event.target.value })} className="mt-2 w-full rounded-xl border border-stone-300 px-3 py-2.5 outline-none ring-emerald-600 focus:ring-2" placeholder="Seu nome" />
            </label>
            <label className="mt-4 block text-sm font-bold text-stone-700">E-mail
              <input required type="email" value={form.email} onChange={(event) => setForm({ ...form, email: event.target.value })} className="mt-2 w-full rounded-xl border border-stone-300 px-3 py-2.5 outline-none ring-emerald-600 focus:ring-2" placeholder="voce@email.com" />
            </label>
            <button disabled={busy} className="mt-7 w-full rounded-xl bg-emerald-800 px-4 py-3 text-sm font-bold text-white transition hover:bg-emerald-700 disabled:opacity-50">{busy ? 'Criando...' : 'Confirmar assinatura'}</button>
          </form>
        </div>
      )}
    </main>
  )
}

export default App
