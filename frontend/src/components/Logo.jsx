export default function Logo({ compact=false }) {
  return <div className="brand"><span className="brand-mark"><i></i><b></b><em></em></span>{!compact && <span>TaskFlow</span>}</div>;
}
