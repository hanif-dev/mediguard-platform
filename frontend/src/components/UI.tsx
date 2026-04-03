import { ReactNode } from 'react'

export function SchwereradBadge({ level }: { level: string }) {
  return <span className={`badge badge-${level?.toLowerCase()}`}>{level}</span>
}
export function StatusBadge({ status }: { status: string }) {
  return <span className={`badge badge-${status?.toLowerCase()}`}>{status?.replace('_', ' ')}</span>
}
export function VersicherungBadge({ art }: { art: string }) {
  return <span className={`badge badge-${art?.toLowerCase()}`}>{art}</span>
}

export function StatCard({ label, value, icon, sub, color = '#1C4ED8' }: {
  label: string; value: string | number; icon: ReactNode; sub?: string; color?: string
}) {
  return (
    <div className="card card-hover p-5">
      <div className="flex items-start justify-between mb-3">
        <span className="text-xs font-medium uppercase tracking-wide" style={{ color: 'var(--text-secondary)' }}>{label}</span>
        <div className="w-8 h-8 rounded-lg flex items-center justify-center" style={{ background: `${color}15` }}>
          <div style={{ color }}>{icon}</div>
        </div>
      </div>
      <div className="text-3xl font-bold" style={{ fontFamily: 'Space Grotesk', color: 'var(--text-primary)' }}>{value}</div>
      {sub && <div className="text-xs mt-1" style={{ color: 'var(--text-secondary)' }}>{sub}</div>}
    </div>
  )
}

export function PageHeader({ title, subtitle, action }: {
  title: string; subtitle?: string; action?: ReactNode
}) {
  return (
    <div className="flex items-center justify-between mb-6">
      <div>
        <h1 className="text-xl font-bold" style={{ fontFamily: 'Space Grotesk', color: 'var(--text-primary)' }}>{title}</h1>
        {subtitle && <p className="text-sm mt-1" style={{ color: 'var(--text-secondary)' }}>{subtitle}</p>}
      </div>
      {action && <div>{action}</div>}
    </div>
  )
}

export function EmptyState({ icon, text }: { icon: ReactNode; text: string }) {
  return (
    <div className="flex flex-col items-center justify-center py-16 gap-3">
      <div style={{ color: 'var(--text-muted)' }}>{icon}</div>
      <p className="text-sm" style={{ color: 'var(--text-muted)' }}>{text}</p>
    </div>
  )
}

export function Loader() {
  return (
    <div className="flex items-center justify-center py-16">
      <div className="w-6 h-6 rounded-full border-2 border-transparent spin"
        style={{ borderTopColor: 'var(--accent)' }} />
    </div>
  )
}
