import { Link } from 'react-router-dom';
import logo from '../assets/logo.png';

const WelcomePage = () => {
    return (
        <div className="flex flex-col items-center justify-center min-h-screen">
            <img src={logo} alt="Logo" className="h-24 mb-8" />
            <div className="flex space-x-4  mb-6">
                <Link
                    to="/login"
                    className="px-6 py-2 bg-gray-900 text-white rounded-lg shadow-md hover:bg-[#32492D] transition-all duration-300 ease-in-out transform"
                >
                    Sign In
                </Link>
                <Link
                    to="/register"
                    className="px-6 py-2 bg-gray-900 text-white rounded-lg shadow-md hover:bg-[#32492D] transition-all duration-300 ease-in-out transform"
                >
                    Sign Up
                </Link>
            </div>
            <p className="text-lg text-gray-900">Please sign in or sign up to continue.</p>
        </div>
    );
};

export default WelcomePage;