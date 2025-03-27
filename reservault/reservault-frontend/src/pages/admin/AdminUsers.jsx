import { useEffect, useState } from "react";
import { motion } from "framer-motion";
import { useQuery, useQueryClient, useMutation } from "@tanstack/react-query";
import { MdVerified } from "react-icons/md";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../../context/AuthContext";
import { FaList, FaTrash } from "react-icons/fa";
import api from "../../api/axios";
import Header from "../../components/common/Header";
import ManagerHotelsModifyModal from "../../components/admin/ManagerHotelsModifyModal";
import DropdownButton from "../../components/common/DropdownButton";

const AdminUsers = () => {
    const { isAuthenticated, isAdmin, loading } = useAuth();
    const navigate = useNavigate();
    const [users, setUsers] = useState([]);
    const queryClient = useQueryClient();
    const [showEditModal, setShowEditModal] = useState(false);
    const [managerId, setManagerId] = useState(null);
    const [currentHotelIdentifiers, setCurrentHotelIdentifiers] = useState([]);

    useEffect(() => {
        if (!loading && (!isAuthenticated || !isAdmin)) {
            navigate("/login");
        }
    }, [loading, isAuthenticated, isAdmin, navigate]);

    const { data: usersData, isLoading, error } = useQuery({
        queryKey: ["admin", "users"],
        queryFn: async () => {
            const { data } = await api.get("/admin/users");
            return data;
        },
        retry: false,
    });

    useEffect(() => {
        if (usersData) {
            setUsers(usersData);
        }
    }, [usersData]);

    const limitText = (text, maxLength) =>
        text.length > maxLength ? text.substring(0, maxLength) + "..." : text;

    const roleLabels = {
        ROLE_USER: { label: "User Account", color: "text-[#32492D]" },
        ROLE_MANAGER: { label: "Manager Account", color: "text-yellow-500" },
        ROLE_ADMIN: { label: "Admin Account", color: "text-red-500" },
    };

    const getDropdownItems = (user) => {
        if (user.roles?.includes("ROLE_ADMIN")) {
            return [];
        }

        const commonItems = [
            {
                label: "Delete",
                icon: FaTrash,
                onClick: () => handleDeleteUser(user.id),
            },
        ];

        if (user.roles?.includes("ROLE_MANAGER")) {
            return [
                {
                    label: "Edit Hotel List",
                    icon: FaList,
                    onClick: () => handleShowManagerHotels(user.id),
                },
                ...(!user.verified
                    ? [
                        {
                            label: "Verify Manager",
                            icon: MdVerified,
                            onClick: () => handleVerifyManager(user.id),
                        },
                    ]
                    : []),
                ...commonItems,
            ];
        }

        return commonItems;
    };

    const handleShowManagerHotels = async (managerId) => {
        try {
            const { data } = await api.get(`/admin/managers/${managerId}/hotels`);
            setManagerId(managerId);
            setCurrentHotelIdentifiers(data.map((hotel) => hotel.hotelIdentifier));
            setShowEditModal(true);
        } catch (error) {
            console.error("Failed to load manager's hotels:", error);
        }
    };

    const handleCloseModal = () => {
        setShowEditModal(false);
        setManagerId(null);
        setCurrentHotelIdentifiers([]);
    };

    const deleteUserMutation = useMutation({
        mutationFn: async (userId) => {
            await api.delete(`/admin/users/${userId}`);
        },
        onSuccess: () => {
            queryClient.invalidateQueries(["admin", "users"]);
        },
    });

    const verifyManagerMutation = useMutation({
        mutationFn: async (userId) => {
            await api.put(`/admin/managers/${userId}/approve`);
        },
        onSuccess: (userId) => {
            queryClient.invalidateQueries(["admin", "users"]);

            setUsers((prevUsers) =>
                prevUsers.map((user) =>
                    user.id === userId ? { ...user, verified: true } : user
                )
            );
        },
    });

    const handleDeleteUser = (userId) => {
        deleteUserMutation.mutate(userId);
    };

    const handleVerifyManager = (userId) => {
        verifyManagerMutation.mutate(userId);
    };

    return (
        <>
            <Header />
            <div className="container mx-auto max-w-6xl py-6">
                <h1 className="text-3xl font-bold text-gray-900 mb-6 text-center">Users</h1>

                {isLoading && <div className="text-center mb-6 text-gray-600">Loading users...</div>}
                {error && <div className="text-center mb-6 text-red-500">Failed to load users.</div>}

                <motion.div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
                    {users?.map((user, index) => (
                        <motion.div
                            key={user.id}
                            className="bg-white shadow-md rounded-lg p-4 w-full border border-gray-200 flex flex-col"
                            initial={{ opacity: 0, y: 30 }}
                            animate={{ opacity: 1, y: 0 }}
                            transition={{ delay: index * 0.1, duration: 0.4, ease: "easeOut" }}
                        >
                            <div className="flex mb-2">
                                <div className="flex space-x-4 w-full">
                                    <div className="w-16 h-16 rounded-full">
                                        <div className={`rounded-full w-full h-auto`}>
                                            {user.profileImage ? (
                                                <img
                                                    src={`${import.meta.env.VITE_API_BASE_URL}/proxy/image?url=${encodeURIComponent(user.profileImage)}`}
                                                    alt={user.email}
                                                    className="rounded-full object-cover"
                                                />
                                            ) : (
                                                <div className="h-16 w-16 bg-[#32492D] text-white flex items-center justify-center rounded-full font-semibold uppercase select-none text-3xl">
                                                    {user?.name ? user.name[0] : "U"}
                                                </div>
                                            )}
                                        </div>
                                    </div>
                                    <div className="flex flex-col justify-center">
                                        <div className="flex space-x-1">
                                            <div className="text-lg font-semibold text-gray-900" title={user.email}>
                                                {limitText(user.email, 17)}
                                            </div>
                                            {user.verified ? (
                                                <MdVerified size={18} className="text-[#32492D]" />
                                            ) : (
                                                <MdVerified size={18} className="text-gray-400" />
                                            )}
                                        </div>
                                        <div className={`text-sm ${roleLabels[user?.roles?.[0]]?.color || "text-gray-500"}`}>
                                            {roleLabels[user?.roles?.[0]]?.label || "User Account"}
                                        </div>
                                    </div>
                                </div>
                                {!user.roles?.includes("ROLE_ADMIN") && (
                                    <div className="ml-auto">
                                        <DropdownButton
                                            itemId={user.id}
                                            menuItems={getDropdownItems(user)}
                                            position="left-0 top-10"
                                        />
                                    </div>
                                )}
                            </div>

                            <div className="flex flex-col mt-2">
                                <div className="text-sm text-gray-600">Name: {user.name}</div>
                                <div className="text-sm text-gray-400">
                                    Created at: {new Date(user.createdAt).toLocaleDateString("en-US", { year: "numeric", month: "2-digit", day: "2-digit" }).replace(/\//g, '.')}
                                </div>
                            </div>
                        </motion.div>
                    ))}
                </motion.div>
            </div>

            {showEditModal && (
                <ManagerHotelsModifyModal
                    managerId={managerId}
                    currentHotelIdentifiers={currentHotelIdentifiers}
                    onClose={handleCloseModal}
                />
            )}
        </>
    );
};

export default AdminUsers;