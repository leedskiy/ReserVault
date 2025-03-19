import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import WelcomePage from './pages/WelcomePage';
import Register from './pages/auth/Register';
import Login from './pages/auth/Login';
import Dashboard from './pages/Dashboard';
import AdminHotels from "./pages/admin/AdminHotels";

const App = () => {
  return (
    <AuthProvider>
      <Router>
        <Routes>
          <Route path="/" element={<WelcomePage />} />
          <Route path="/register" element={<Register />} />
          <Route path="/login" element={<Login />} />
          <Route path="/dashboard" element={<Dashboard />} />
          <Route path="/profile" element={<div>Profile Page (to be implemented)</div>} />
          <Route path="/admin/hotels/:view" element={<AdminHotels />} />
          <Route path="/admin/hotels" element={<AdminHotels />} />
        </Routes>
      </Router>
    </AuthProvider>
  );
};

export default App;
