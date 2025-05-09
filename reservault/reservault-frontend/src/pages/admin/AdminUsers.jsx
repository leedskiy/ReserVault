import { useEffect, useState, useRef } from "react";
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
import ConfirmationPopup from "../../components/common/ConfirmationPopup";
import Pagination from "../../components/common/Pagination";

const AdminUsers = () => {
    const { isAuthenticated, isAdmin, loading } = useAuth();
    const navigate = useNavigate();
    const [users, setUsers] = useState([]);
    const queryClient = useQueryClient();
    const [showEditModal, setShowEditModal] = useState(false);
    const [managerId, setManagerId] = useState(null);
    const [currentHotelIdentifiers, setCurrentHotelIdentifiers] = useState([]);
    const [userToDelete, setUserToDelete] = useState(null);

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
        if (user.roles?.includes("ROLE_ADMIN")) return [];

        const verificationStatus = getVerificationStatus(user.id, user.verified);
        const showVerify = user.roles.includes("ROLE_MANAGER") &&
            (verificationStatus === "unverified" || verificationStatus === "partially_verified");

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
                ...(showVerify
                    ? [{
                        label: "Verify Manager",
                        icon: MdVerified,
                        onClick: () => handleVerifyManager(user.id),
                    }]
                    : []),
                ...commonItems,
            ];
        }

        return commonItems;
    };

    const { data: hotelManagerStatusMap = {} } = useQuery({
        queryKey: ["admin", "managerHotelStatuses"],
        queryFn: async () => {
            const { data: users } = await api.get("/admin/users");
            const map = {};

            await Promise.all(
                users
                    .filter((user) => user.roles.includes("ROLE_MANAGER"))
                    .map(async (user) => {
                        const { data } = await api.get(`/admin/managers/${user.id}/hotels`);
                        map[user.id] = data;
                    })
            );

            return map;
        },
        enabled: !!usersData,
    });

    const getVerificationStatus = (userId, isVerified) => {
        const relations = hotelManagerStatusMap[userId];
        if (!relations || relations.length === 0) return "unknown";

        const allApproved = relations.every((hm) => hm.status === "APPROVED");

        if (!isVerified) return "unverified";
        if (allApproved) return "fully_verified";
        return "partially_verified";
    };

    const handleShowManagerHotels = async (managerId) => {
        try {
            const { data } = await api.get(`/admin/managers/${managerId}/hotels`);
            setManagerId(managerId);
            setCurrentHotelIdentifiers(data);
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
        setUserToDelete(userId);
    };

    const handleVerifyManager = (userId) => {
        verifyManagerMutation.mutate(userId);
    };

    const [currentPage, setCurrentPage] = useState(1);
    const usersPerPage = 12;

    const totalPages = Math.ceil((users?.length || 0) / usersPerPage);
    const paginatedUsers = users?.slice((currentPage - 1) * usersPerPage, currentPage * usersPerPage);

    const listRef = useRef(null);

    const handlePageChange = (pageNum) => {
        if (pageNum >= 1 && pageNum <= totalPages) {
            setCurrentPage(pageNum);
            if (listRef.current) {
                listRef.current.scrollIntoView({ behavior: "smooth" });
            }
        }
    };

    return (
        <>
            <Header />
            <div className="container mx-auto max-w-6xl py-6">
                <h1 className="text-3xl font-bold text-gray-900 mb-6 text-center">Users</h1>

                {isLoading && <div className="text-center mb-6 text-gray-600">Loading users...</div>}
                {error && <div className="text-center mb-6 text-red-500">Failed to load users.</div>}

                <motion.div ref={listRef} className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
                    {paginatedUsers?.map((user, index) => (
                        <motion.div
                            key={user.id}
                            className="bg-white shadow-md rounded-lg p-4 w-full border border-gray-200 flex flex-col"
                            initial={{ opacity: 0, y: 30 }}
                            animate={{ opacity: 1, y: 0 }}
                            transition={{ delay: index * 0.1, duration: 0.2, ease: "easeOut" }}
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

                                            {user.roles?.includes("ROLE_MANAGER") ? (
                                                <MdVerified
                                                    size={18}
                                                    title={
                                                        getVerificationStatus(user.id, user.verified) === "fully_verified"
                                                            ? "Manager is fully verified"
                                                            : getVerificationStatus(user.id, user.verified) === "partially_verified"
                                                                ? "Manager is verified, some hotels pending approval"
                                                                : "Manager is not verified"
                                                    }
                                                    className={
                                                        getVerificationStatus(user.id, user.verified) === "fully_verified"
                                                            ? "text-[#32492D]"
                                                            : getVerificationStatus(user.id, user.verified) === "partially_verified"
                                                                ? "text-yellow-500"
                                                                : "text-gray-400"
                                                    }
                                                />
                                            ) : (
                                                user.verified ? (
                                                    <MdVerified size={18} className="text-[#32492D]" title="Verified" />
                                                ) : (
                                                    <MdVerified size={18} className="text-gray-400" title="Not Verified" />
                                                )
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

            <Pagination
                currentPage={currentPage}
                totalPages={totalPages}
                onPageChange={handlePageChange}
            />

            {showEditModal && (
                <ManagerHotelsModifyModal
                    managerId={managerId}
                    currentHotelIdentifiers={currentHotelIdentifiers}
                    onClose={handleCloseModal}
                />
            )}

            {userToDelete && (
                <ConfirmationPopup
                    isOpen={!!userToDelete}
                    title="Delete User"
                    message={"Are you sure you want to delete this user? Action cannot be undone, and all associated " +
                        "data will be lost. It will delete associated offers, hotemanagaers, bookings, payments etc."
                    }
                    onConfirm={() => {
                        deleteUserMutation.mutate(userToDelete, {
                            onSuccess: () => {
                                queryClient.invalidateQueries(["admin", "users"]);
                                setUserToDelete(null);
                            },
                        });
                    }}
                    onCancel={() => setUserToDelete(null)}
                    confirmLabel="Delete"
                    cancelLabel="Cancel"
                />
            )}
        </>
    );
};

export default AdminUsers;