import { useState, useEffect } from "react";
import { motion } from "framer-motion";
import { useAuth } from "../../../context/AuthContext";
import api from "../../../api/axios";

const ProfileNameSection = () => {
    const { user, refetchUser } = useAuth();
    const [name, setName] = useState("");
    const [loading, setLoading] = useState(false);
    const [message, setMessage] = useState(null);
    const [messageType, setMessageType] = useState(null);

    useEffect(() => {
        if (user?.name) {
            setName(user.name);
        }
    }, [user]);

    const googleImageUrl = user?.profileImage;
    const proxiedImageUrl = googleImageUrl
        ? `${import.meta.env.VITE_API_BASE_URL}/proxy/image?url=${encodeURIComponent(googleImageUrl)}`
        : null;

    const roleLabels = {
        ROLE_USER: { label: "User Account", border: "border-[#32492D]", color: "text-[#32492D]" },
        ROLE_MANAGER: { label: "Manager Account", border: "border-yellow-500", color: "text-yellow-500" },
        ROLE_ADMIN: { label: "Admin Account", border: "border-red-500", color: "text-red-500" },
    };

    const userRole = user?.roles?.[0] || "ROLE_USER";
    const roleLabel = roleLabels[userRole]?.label;
    const roleColor = roleLabels[userRole]?.color;
    const roleBorder = roleLabels[userRole]?.border;

    const handleSubmit = async () => {
        if (!name.trim()) {
            setMessage("Name cannot be empty.");
            setMessageType("error");
            return;
        }

        try {
            setLoading(true);
            setMessage(null);
            await api.put(`/users/me/name?name=${encodeURIComponent(name.trim())}`);
            await refetchUser();
            setMessage("Name updated successfully.");
            setMessageType("success");
        } catch (err) {
            setMessage("Failed to update name.");
            setMessageType("error");
        } finally {
            setLoading(false);
        }
    };

    return (
        <motion.div
            className="bg-white shadow-md rounded-lg border border-gray-200 p-10 w-full relative items-center justify-center flex flex-col space-y-6"
            initial={{ opacity: 0, y: 30 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ delay: 0.1, duration: 0.4, ease: "easeOut" }}
        >
            <div className="flex items-center justify-center gap-4 mb-2">
                <div className="relative flex items-center justify-center h-16 w-16">
                    <div className={`absolute inset-0 rounded-full border-2 ${roleBorder}`}></div>
                    {proxiedImageUrl ? (
                        <img src={proxiedImageUrl} alt="Profile" className="h-14 w-14 rounded-full object-cover" />
                    ) : (
                        <div className="h-14 w-14 bg-[#32492D] text-white flex items-center justify-center rounded-full font-semibold uppercase text-3xl">
                            {user?.name?.[0] || "U"}
                        </div>
                    )}
                </div>

                <div>
                    <div className="text-lg font-semibold text-gray-900">{user?.email}</div>
                    <div className={`text-sm ${roleColor}`}>{roleLabel}</div>
                </div>
            </div>

            <div className="items-center justify-center flex flex-col w-full">
                <label className="block text-gray-600 font-medium mb-1">Name</label>
                <input
                    type="text"
                    className="w-1/2 border border-gray-300 rounded-md px-3 py-2
                                    focus:outline-none transition-all duration-100 ease-in-out transform
                                    rounded-lg focus:outline-none focus:border-[#32492D] focus:ring-1 focus:ring-[#32492D]"
                    value={name}
                    onChange={(e) => setName(e.target.value)}
                />
            </div>

            <button
                onClick={handleSubmit}
                disabled={loading}
                className="items-center justify-center px-4 py-2 text-white rounded-lg bg-[#32492D] hover:bg-[#273823] transition-all duration-300 ease-in-out transform"
            >
                {loading ? "Updating..." : "Update Name"}
            </button>

            {message && (
                <div
                    className={`mt-3 text-sm ${messageType === "success" ? "text-[#32492D]" : "text-red-500"
                        }`}
                >
                    {message}
                </div>
            )}
        </motion.div>
    );
};

export default ProfileNameSection;
