import { Link, useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import { useState, useEffect, useRef } from "react";
import { FaUser, FaHotel, FaSignOutAlt } from "react-icons/fa";
import { motion, AnimatePresence } from "framer-motion";
import logo from "../assets/logo.png";

const Header = () => {
    const { user, logout, isAdmin } = useAuth();
    const [menuOpen, setMenuOpen] = useState(false);
    const menuRef = useRef(null);
    const buttonRef = useRef(null);
    const navigate = useNavigate();

    const googleImageUrl = user?.profileImage;
    const proxiedImageUrl = googleImageUrl
        ? `${import.meta.env.VITE_API_BASE_URL}/proxy/image?url=${encodeURIComponent(googleImageUrl)}`
        : null;

    const maxNameLength = 25;
    const displayedName =
        user?.name.length > maxNameLength ? user.name.substring(0, maxNameLength) + "..." : user?.name;

    const roleLabels = {
        ROLE_USER: { label: "User Account", color: "text-gray-500", border: "border-gray-500" },
        ROLE_MANAGER: { label: "Manager Account", color: "text-yellow-500", border: "border-yellow-500" },
        ROLE_ADMIN: { label: "Admin Account", color: "text-red-500", border: "border-red-500" },
    };

    const userRole = user?.roles?.[0] || "ROLE_USER";
    const roleDisplay = roleLabels[userRole]?.label || "User Account";
    const roleColor = roleLabels[userRole]?.color || "text-gray-500";
    const roleBorder = roleLabels[userRole]?.border || "border-gray-500";

    const toggleMenu = () => {
        setMenuOpen((prev) => !prev);
    };

    useEffect(() => {
        const handleClickOutside = (event) => {
            if (menuRef.current && !menuRef.current.contains(event.target)) {
                setMenuOpen(false);
            }
        };

        if (menuOpen) {
            document.addEventListener("mousedown", handleClickOutside);
        } else {
            document.removeEventListener("mousedown", handleClickOutside);
        }

        return () => {
            document.removeEventListener("mousedown", handleClickOutside);
        };
    }, [menuOpen]);

    const handleLogout = async () => {
        await logout(navigate);
        setMenuOpen(false);
    };

    return (
        <header className="bg-white shadow-md py-3 px-8">
            <div className="container max-w-6xl mx-auto flex justify-between items-center">
                <Link to="/dashboard">
                    <img src={logo} alt="Logo" className="h-10 cursor-pointer" />
                </Link>

                <div className="relative" ref={menuRef}>
                    <button
                        ref={buttonRef}
                        onClick={toggleMenu}
                        className="flex items-center space-x-2 focus:outline-none duration-200 hover:bg-gray-200 py-2 px-4 rounded-lg min-w-[12rem] justify-between"
                        style={{ minWidth: "14rem" }}
                    >
                        <div className="relative flex items-center justify-center h-11 w-11">
                            <div className={`absolute inset-0 rounded-full border-2 ${roleBorder}`}></div>
                            {user?.profileImage ? (
                                <img
                                    src={proxiedImageUrl}
                                    alt="User"
                                    className="h-9 w-9 rounded-full object-cover"
                                />
                            ) : (
                                <div className="h-9 w-9 bg-[#32492D] text-white flex items-center justify-center rounded-full font-semibold uppercase select-none text-xl">
                                    {user?.name ? user.name[0] : "U"}
                                </div>
                            )}
                        </div>

                        <div className="text-left flex-1">
                            <span className="text-gray-900 font-semibold block">{displayedName}</span>
                            <span className={`text-sm ${roleColor}`}>{roleDisplay}</span>
                        </div>
                    </button>

                    <AnimatePresence>
                        {menuOpen && (
                            <motion.div
                                className="absolute right-0 mt-2 bg-white border rounded-lg shadow-lg"
                                style={{ minWidth: buttonRef.current ? buttonRef.current.offsetWidth : "14rem" }}
                                initial={{ opacity: 0, y: -10 }}
                                animate={{ opacity: 1, y: 0 }}
                                exit={{ opacity: 0, y: -10 }}
                                transition={{ duration: 0.2, ease: "easeOut" }}
                            >
                                <Link
                                    to="/profile"
                                    className="flex items-center px-4 py-2 text-gray-800 hover:bg-gray-200 active:bg-gray-300 rounded-t-lg transition-all duration-200 ease-in-out"
                                    onClick={() => setMenuOpen(false)}
                                >
                                    <FaUser className="mr-2" />
                                    Profile
                                </Link>

                                {isAdmin && (
                                    <Link
                                        to="/admin/hotels/list"
                                        className="flex items-center px-4 py-2 text-gray-800 hover:bg-gray-200 active:bg-gray-300 transition-all duration-200 ease-in-out"
                                        onClick={() => setMenuOpen(false)}
                                    >
                                        <FaHotel className="mr-2" />
                                        Hotels
                                    </Link>
                                )}

                                <button
                                    onClick={handleLogout}
                                    className="w-full flex items-center px-4 py-2 text-gray-800 hover:bg-gray-200 active:bg-gray-300 rounded-b-lg transition-all duration-200 ease-in-out"
                                >
                                    <FaSignOutAlt className="mr-2" />
                                    Logout
                                </button>
                            </motion.div>
                        )}
                    </AnimatePresence>
                </div>
            </div>
        </header>
    );
};

export default Header;
