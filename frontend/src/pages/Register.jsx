import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { register } from '../services/authService';
import { errorMessage } from '../services/api';
export default function Register() {
  const [error, setError] = useState(''); const [busy, setBusy] = useState(false); const navigate = useNavigate();
  async function submit(event) {
    event.preventDefault(); setBusy(true); setError('');
    const input = Object.fromEntries(new FormData(event.currentTarget));
    if (input.password !== input.confirmation) { setError('Les mots de passe ne correspondent pas.'); setBusy(false); return; }
    delete input.confirmation;
    try { await register(input); navigate('/login?registered=1'); } catch (e) { setError(errorMessage(e)); } finally { setBusy(false); }
  }
  return <section className="single"><h1>Créer un compte</h1><p>Votre compte vous donne accès à l’espace employé.</p>
    {error && <p className="error" role="alert">{error}</p>}
    <form onSubmit={submit}><label>Nom complet<input name="fullName" autoComplete="name" required maxLength={100}/></label><label>Email<input name="email" type="email" autoComplete="email" required maxLength={254}/></label>
      <label>Mot de passe<input name="password" type="password" autoComplete="new-password" required minLength={8} maxLength={72} aria-describedby="password-help"/></label><small id="password-help">Au moins 8 caractères, au maximum 72 octets UTF-8.</small>
      <label>Confirmer le mot de passe<input name="confirmation" type="password" autoComplete="new-password" required minLength={8} maxLength={72}/></label><button disabled={busy}>{busy ? 'Création en cours…' : 'Créer mon compte'}</button></form>
    <p className="form-foot">Déjà inscrit ? <Link to="/login">Se connecter</Link></p></section>;
}
