export default function StatusPill({value}){return <span className={`pill ${String(value).toLowerCase()}`}>{String(value).replaceAll('_',' ')}</span>;}
