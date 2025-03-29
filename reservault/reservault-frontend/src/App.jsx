import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import WelcomePage from './pages/common/WelcomePage';
import Register from './pages/auth/Register';
import Login from './pages/auth/Login';
import Dashboard from './pages/common/Dashboard';
import AdminHotels from "./pages/admin/AdminHotels";
import AdminUsers from "./pages/admin/AdminUsers";
import ManagerOffers from "./pages/manager/ManagerOffers";
import OfferSearchResults from "./pages/user/OfferSearchResults";
import UserBookings from "./pages/user/UserBookings";

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
          <Route path="/admin/users" element={<AdminUsers />} />
          <Route path="/manager/offers" element={<ManagerOffers />} />
          <Route path="/manager/offers/:view" element={<ManagerOffers />} />
          <Route path="/offers/search" element={<OfferSearchResults />} />
          <Route path="/user/bookings" element={<UserBookings />} />
        </Routes>
      </Router>
    </AuthProvider>
  );
};

export default App;
