import { Link } from 'react-router-dom';

export default function NotFoundPage() {
  return <div className="not-found"><span className="eyebrow">404</span><h1>Page not found</h1><p>The requested TaskFlow page does not exist.</p><Link className="primary-link" to="/">Return home</Link></div>;
}
