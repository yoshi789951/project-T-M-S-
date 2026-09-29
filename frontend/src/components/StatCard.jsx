export default function StatCard({label,value,meta,accent='blue'}){return <div className={`stat-card ${accent}`}><span>{label}</span><strong>{value ?? 0}</strong><small>{meta}</small></div>;}
