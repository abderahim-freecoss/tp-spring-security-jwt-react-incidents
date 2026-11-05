import { BrowserRouter, Routes, Route, Navigate, Link } from 'react-router-dom';
import Navbar from './components/Navbar';
import ProtectedRoute from './components/ProtectedRoute';
import Login from './pages/Login';
import Register from './pages/Register';
import Profile from './pages/Profile';
import RoleRoute from './components/RoleRoute';
import { Dashboard, IncidentList, NewIncident, IncidentDetail, AdminUsers } from './pages/Incidents';
export default function App() {
  return <BrowserRouter><a className="skip" href="#main">Aller au contenu</a><Navbar/><main id="main"><Routes>
    <Route path="/login" element={<Login/>}/><Route path="/register" element={<Register/>}/>
    <Route element={<ProtectedRoute/>}><Route path="/profile" element={<Profile/>}/><Route path="/dashboard" element={<Dashboard/>}/><Route path="/incidents" element={<IncidentList/>}/><Route path="/incidents/:id" element={<IncidentDetail/>}/><Route element={<RoleRoute roles={['EMPLOYEE']}/>}><Route path="/incidents/new" element={<NewIncident/>}/></Route><Route element={<RoleRoute roles={['ADMIN']}/>}><Route path="/admin/users" element={<AdminUsers/>}/></Route></Route>
    <Route path="/" element={<Navigate to="/dashboard" replace/>}/>
    <Route path="*" element={<section className="single"><h1>Page introuvable</h1><Link to="/profile">Revenir à mon espace</Link></section>}/>
  </Routes></main><footer>Suivi des incidents · Espace personnel</footer></BrowserRouter>;
}
