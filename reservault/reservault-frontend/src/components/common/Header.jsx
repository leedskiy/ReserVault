import { Link, useNavigate } from "react-router-dom";
import { useAuth } from "../../context/AuthContext";
import { useState, useEffect, useRef } from "react";
import { FaUser, FaUsers, FaHotel, FaSignOutAlt } from "react-icons/fa";
import DropdownMenu from "./DropdownMenu";
import logo from "../../assets/logo.png";

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
        ROLE_USER: { label: "User Account", color: "text-[#32492D]", border: "border-[#32492D]" },
        ROLE_MANAGER: { label: "Manager Account", color: "text-yellow-500", border: "border-yellow-500" },
        ROLE_ADMIN: { label: "Admin Account", color: "text-red-500", border: "border-red-500" },
    };

    const userRole = user?.roles?.[0] || "ROLE_USER";
    const roleDisplay = roleLabels[userRole]?.label || "User Account";
    const roleColor = roleLabels[userRole]?.color || "text-gray-500";
    const roleBorder = roleLabels[userRole]?.border || "border-gray-500";

    const handleLogout = async () => {
        await logout(navigate);
    };

    const menuItems = [
        { label: "Profile", icon: FaUser, onClick: () => navigate("/profile") },
        isAdmin && { label: "Hotels", icon: FaHotel, onClick: () => navigate("/admin/hotels/list") },
        isAdmin && { label: "Users", icon: FaUsers, onClick: () => navigate("/admin/users") },
        { label: "Logout", icon: FaSignOutAlt, onClick: handleLogout },
    ].filter(Boolean);

    useEffect(() => {
        const handleClickOutside = (event) => {
            if (menuRef.current && !menuRef.current.contains(event.target)) {
                setMenuOpen(false);
            }
        };

        document.addEventListener("mousedown", handleClickOutside);
        return () => document.removeEventListener("mousedown", handleClickOutside);
    }, []);

    return (
        <header className="bg-white shadow-md py-3 px-8 z-50">
            <div className="container max-w-6xl mx-auto flex justify-between items-center">
                <Link to="/dashboard">
                    <img src={logo} alt="Logo" className="h-10 cursor-pointer" />
                </Link>

                <div className="relative" ref={menuRef}>
                    <button
                        ref={buttonRef}
                        onClick={() => setMenuOpen((prev) => !prev)}
                        className="flex items-center space-x-2 focus:outline-none duration-200 hover:bg-gray-200 py-2 px-4 rounded-lg min-w-[12rem] justify-between"
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

                    <DropdownMenu isOpen={menuOpen} onClose={() => setMenuOpen(false)} menuItems={menuItems} />
                </div>
            </div>
        </header>
    );
};

export default Header;