import { useState } from 'react';
import { Link, useNavigate, useSearchParams } from 'react-router-dom';
import { login } from '../services/authService';
import { errorMessage } from '../services/api';
export default function Login() {
  const [error, setError] = useState(''); const [busy, setBusy] = useState(false);
  const navigate = useNavigate(); const [params] = useSearchParams();
  async function submit(event) {
    event.preventDefault(); setBusy(true); setError('');
    try { await login(Object.fromEntries(new FormData(event.currentTarget))); navigate('/profile'); }
    catch (e) { setError(errorMessage(e)); } finally { setBusy(false); }
  }
  return <section className="auth-layout"><div className="intro"><h1>Votre espace <br/>d’assistance.</h1><p>Connectez-vous à votre compte pour accéder à votre espace personnel.</p><p className="quiet">Un accès adapté à votre rôle : employé, technicien ou administrateur.</p></div>
    <div className="form-panel"><h2>Se connecter</h2><p>Renseignez vos identifiants professionnels.</p>
      {params.has('expired') && <p className="notice" role="status">Votre session a expiré ou n’est plus valide. Reconnectez-vous.</p>}
      {params.has('updated') && <p className="notice" role="status">Modification enregistrée. Connectez-vous avec vos nouveaux identifiants.</p>}
      {params.has('registered') && <p className="notice" role="status">Votre compte employé est créé. Vous pouvez vous connecter.</p>}
      {error && <p className="error" role="alert">{error}</p>}
      <form onSubmit={submit}><label>Email<input name="email" type="email" autoComplete="username" required maxLength={254}/></label><label>Mot de passe<input name="password" type="password" autoComplete="current-password" required maxLength={72}/></label><button disabled={busy}>{busy ? 'Connexion en cours…' : 'Se connecter'}</button></form>
      <p className="form-foot">Première visite ? <Link to="/register">Créer un compte</Link></p>
    </div></section>;
}
