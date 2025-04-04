import { useState } from "react";
import { useForm } from "react-hook-form";
import { motion } from "framer-motion";
import { FaExclamationTriangle } from "react-icons/fa";
import api from "../../../api/axios";
import { useAuth } from "../../../context/AuthContext";
import { useNavigate } from "react-router-dom";
import ConfirmationPopup from "../../common/ConfirmationPopup";

const ProfileSecuritySection = () => {
    const [loading, setLoading] = useState(false);
    const [successMessage, setSuccessMessage] = useState("");
    const [showDeleteConfirm, setShowDeleteConfirm] = useState(false);

    const { logout, user } = useAuth();
    const navigate = useNavigate();

    const {
        register,
        handleSubmit,
        watch,
        setError,
        formState: { errors },
        reset,
    } = useForm();

    const onSubmit = async (data) => {
        setSuccessMessage("");
        try {
            setLoading(true);
            await api.put("/auth/me/password", {
                currentPassword: data.currentPassword,
                newPassword: data.newPassword,
            });
            setSuccessMessage("Password updated successfully.");
            reset();
        } catch (err) {
            const msg = err.response?.data?.message || "Failed to update password.";
            if (msg.toLowerCase().includes("current")) {
                setError("currentPassword", { message: msg });
            }
        } finally {
            setLoading(false);
        }
    };

    const handleDeleteAccount = async () => {
        try {
            await api.delete("/users/me");
            await logout(navigate);
        } catch (err) { }
    };

    return (
        <>
            <motion.div
                className="bg-white shadow-md rounded-lg border border-gray-200 p-10 w-full relative items-center justify-center flex flex-col space-y-10"
                initial={{ opacity: 0, y: 30 }}
                animate={{ opacity: 1, y: 0 }}
                transition={{ delay: 0.1, duration: 0.4, ease: "easeOut" }}
            >
                {user?.authProvider !== "GOOGLE" && (
                    <form
                        onSubmit={handleSubmit(onSubmit)}
                        className="w-full flex flex-col relative items-center justify-center space-y-6"
                    >
                        <h2 className="text-2xl font-semibold text-gray-900 text-center">Password Change</h2>

                        <div className="items-center justify-center flex flex-col w-full">
                            <label className="block text-gray-600 font-medium mb-1">Current Password</label>
                            <input
                                type="password"
                                {...register("currentPassword", { required: "Current password is required" })}
                                className="w-1/2 border border-gray-300 rounded-md px-3 py-2
                                        focus:outline-none transition-all duration-100 ease-in-out transform
                                        rounded-lg focus:outline-none focus:border-[#32492D] focus:ring-1 focus:ring-[#32492D]"
                            />
                            {errors.currentPassword && (
                                <p className="text-red-500 text-sm mt-1">{errors.currentPassword.message}</p>
                            )}
                        </div>

                        <div className="items-center justify-center flex flex-col w-full">
                            <label className="block text-gray-600 font-medium mb-1">New Password</label>
                            <input
                                type="password"
                                className="w-1/2 border border-gray-300 rounded-md px-3 py-2
                                        focus:outline-none transition-all duration-100 ease-in-out transform
                                        rounded-lg focus:outline-none focus:border-[#32492D] focus:ring-1 focus:ring-[#32492D]"
                                {...register("newPassword", {
                                    required: "New password is required",
                                    minLength: {
                                        value: 6,
                                        message: "Password must be at least 6 characters",
                                    },
                                })}
                            />
                            {errors.newPassword && (
                                <p className="text-red-500 text-sm mt-1">{errors.newPassword.message}</p>
                            )}
                        </div>

                        <div className="items-center justify-center flex flex-col w-full">
                            <label className="block text-gray-600 font-medium mb-1">Confirm New Password</label>
                            <input
                                type="password"
                                className="w-1/2 border border-gray-300 rounded-md px-3 py-2
                                        focus:outline-none transition-all duration-100 ease-in-out transform
                                        rounded-lg focus:outline-none focus:border-[#32492D] focus:ring-1 focus:ring-[#32492D]"
                                {...register("confirmPassword", {
                                    required: "Please confirm your new password",
                                    validate: (value) =>
                                        value === watch("newPassword") || "Passwords do not match",
                                })}
                            />
                            {errors.confirmPassword && (
                                <p className="text-red-500 text-sm mt-1">{errors.confirmPassword.message}</p>
                            )}
                        </div>

                        <button
                            type="submit"
                            disabled={loading}
                            className="items-center justify-center px-4 py-2 text-white rounded-lg bg-[#32492D] hover:bg-[#273823] transition-all duration-300 ease-in-out transform"
                        >
                            {loading ? "Updating..." : "Update Password"}
                        </button>

                        {successMessage && (
                            <p className="text-green-600 text-sm text-center mt-2">{successMessage}</p>
                        )}
                    </form>
                )}

                {!user?.roles?.includes("ROLE_ADMIN") && (
                    <div className="w-full flex flex-col relative items-center justify-center space-y-8">
                        <h2 className="text-2xl font-semibold text-gray-900 text-center">Delete Account</h2>

                        <p className="text-sm text-gray-600">
                            Deleting your account is permanent and cannot be undone.
                        </p>
                        <button
                            onClick={() => setShowDeleteConfirm(true)}
                            className="w-1/2 flex items-center justify-center px-4 py-2 border rounded-lg bg-white shadow-md hover:bg-gray-200 transition-all duration-300"
                        >
                            Delete My Account
                        </button>
                    </div>
                )}
            </motion.div>

            <ConfirmationPopup
                isOpen={showDeleteConfirm}
                title="Delete Account"
                message="Are you sure you want to delete your account? This action is irreversible and all your associated data will be lost."
                icon={FaExclamationTriangle}
                confirmLabel="Delete"
                cancelLabel="Cancel"
                onCancel={() => setShowDeleteConfirm(false)}
                onConfirm={async () => {
                    try {
                        await api.delete("/users/me");
                        await logout(navigate);
                    } catch (err) {
                        console.error("Failed to delete account:", err);
                    } finally {
                        setShowDeleteConfirm(false);
                    }
                }}
            />
        </>
    );
};

export default ProfileSecuritySection;
