import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import api, { errorMessage } from '../services/api';
import { logout } from '../services/authService';
const roles = { EMPLOYEE: 'Employé', TECHNICIAN: 'Technicien', ADMIN: 'Administrateur' };
export default function Profile() {
  const [user, setUser] = useState(null); const [error, setError] = useState(''); const [message, setMessage] = useState(''); const [busy, setBusy] = useState(false); const navigate = useNavigate();
  async function load() { setError(''); try { setUser((await api.get('/api/profile')).data); } catch (e) { setError(errorMessage(e)); } }
  useEffect(() => { load(); }, []);
  function reconnect() { logout(); navigate('/login?updated=1'); }
  async function save(event, password = false) {
    event.preventDefault(); const form = event.currentTarget; const input = Object.fromEntries(new FormData(form)); setError(''); setMessage(''); setBusy(true);
    try {
      if (password) { await api.put('/api/profile/password', input); reconnect(); }
      else { const { data } = await api.put('/api/profile', input); if (data.email !== user.email) reconnect(); else { setUser(data); setMessage('Votre profil a été enregistré.'); } }
    } catch (e) { setError(errorMessage(e)); } finally { setBusy(false); }
  }
  if (!user) return <section className="single">{error ? <><p role="alert" className="error">{error}</p><button onClick={load}>Réessayer</button></> : <p role="status">Chargement du profil…</p>}</section>;
  return <section className="profile"><div className="page-heading"><h1>Mon profil</h1><p>Gérez vos coordonnées et la sécurité de votre compte.</p><span className="role">{roles[user.role]}</span></div>
    {error && <p role="alert" className="error">{error}</p>}{message && <p role="status" className="notice">{message}</p>}
    <div className="profile-grid"><section><h2>Informations personnelles</h2><p>Un changement d’email nécessite une nouvelle connexion.</p><form onSubmit={save}><label>Nom complet<input name="fullName" defaultValue={user.fullName} required maxLength={100} autoComplete="name"/></label><label>Email<input name="email" type="email" defaultValue={user.email} required maxLength={254} autoComplete="email"/></label><button disabled={busy}>Enregistrer les modifications</button></form></section>
    <section><h2>Changer de mot de passe</h2><p>Vos sessions seront fermées après la modification.</p><form onSubmit={e => save(e, true)}><label>Ancien mot de passe<input name="oldPassword" type="password" autoComplete="current-password" required maxLength={72}/></label><label>Nouveau mot de passe<input name="newPassword" type="password" autoComplete="new-password" required minLength={8} maxLength={72}/></label><label>Confirmer le nouveau mot de passe<input name="confirmation" type="password" autoComplete="new-password" required minLength={8} maxLength={72}/></label><button className="secondary" disabled={busy}>Modifier le mot de passe</button></form></section></div>
  </section>;
}
