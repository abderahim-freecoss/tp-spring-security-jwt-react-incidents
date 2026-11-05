import { NavLink, useNavigate } from 'react-router-dom';
import { getSession, logout } from '../services/authService';
export default function Navbar() {
  const navigate = useNavigate();
  return <header className="topbar"><NavLink className="brand" to="/profile">Suivi des incidents<span>Assistance informatique</span></NavLink>
    <nav aria-label="Navigation principale">{getSession()?.token ? <><NavLink to="/dashboard">Tableau de bord</NavLink><NavLink to="/incidents">Incidents</NavLink>{getSession()?.role === 'ADMIN' && <NavLink to="/admin/users">Utilisateurs</NavLink>}<NavLink to="/profile">Mon profil</NavLink><button className="secondary" onClick={() => { logout(); navigate('/login'); }}>Se déconnecter</button></> : <NavLink to="/login">Connexion</NavLink>}</nav>
  </header>;
}
