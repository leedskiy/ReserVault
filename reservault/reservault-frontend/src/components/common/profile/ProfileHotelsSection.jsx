import { useEffect, useState } from "react";
import { FaTimes } from "react-icons/fa";
import { motion } from "framer-motion";
import { useMutation, useQuery } from "@tanstack/react-query";
import { FaExclamationTriangle } from "react-icons/fa";
import api from "../../../api/axios";
import { useAuth } from "../../../context/AuthContext";
import PopupModal from "../../common/PopupModal";

const ProfileHotelsSection = () => {
    const { user, loading } = useAuth();
    const [hotelIdentifier, setHotelIdentifier] = useState("");
    const [hotelManagers, setHotelManagers] = useState([]);
    const [error, setError] = useState("");
    const [success, setSuccess] = useState("");
    const [showPendingModal, setShowPendingModal] = useState(false);
    const [pendingAdded, setPendingAdded] = useState([]);

    const { data: currentHotels, refetch } = useQuery({
        queryKey: ["manager", "hotels"],
        queryFn: async () => {
            const { data } = await api.get("/manager/hotels");
            return data;
        },
        enabled: !!user,
    });

    useEffect(() => {
        if (currentHotels) {
            setHotelManagers(currentHotels);
        }
    }, [currentHotels]);

    const addHotelIdentifier = () => {
        if (
            hotelIdentifier &&
            !hotelManagers.some((hm) => hm.hotelIdentifier === hotelIdentifier)
        ) {
            setHotelManagers([
                ...hotelManagers,
                { hotelIdentifier, status: "PENDING" },
            ]);
            setHotelIdentifier("");
        }
    };

    const removeHotelIdentifier = (identifier) => {
        setHotelManagers(hotelManagers.filter((hm) => hm.hotelIdentifier !== identifier));
    };

    const updateHotelsMutation = useMutation({
        mutationFn: async () => {
            await api.put("/manager/hotels", hotelManagers.map((hm) => hm.hotelIdentifier));
        },
        onSuccess: () => {
            setSuccess("Hotel list updated successfully.");
            setError("");
            refetch();
        },
        onError: (err) => {
            setSuccess("");
            setError(
                err.response?.data || "An error occurred while updating hotel list."
            );
        },
    });

    const handleSave = async () => {
        const before = currentHotels?.map((h) => h.hotelIdentifier) || [];

        await updateHotelsMutation.mutateAsync();

        const newlyPending = hotelManagers.filter(
            (hm) =>
                !before.includes(hm.hotelIdentifier) &&
                hm.status === "PENDING"
        );

        if (newlyPending.length > 0) {
            setPendingAdded(newlyPending.map((h) => h.hotelIdentifier));
            setShowPendingModal(true);
        }
    };

    if (loading) return <div>Loading...</div>;

    return (
        <>
            <motion.div
                className="bg-white shadow-md rounded-lg border border-gray-200 p-10 w-full relative items-center justify-center flex flex-col space-y-5"
                initial={{ opacity: 0, y: 30 }}
                animate={{ opacity: 1, y: 0 }}
                transition={{ delay: 0.1, duration: 0.4, ease: "easeOut" }}
            >
                <h3 className="text-xl font-semibold text-gray-900 text-center">Manage Your Hotels List</h3>

                {error && (
                    <div className="text-red-500 text-sm mb-4 text-center">{error}</div>
                )}
                {success && (
                    <div className="text-green-600 text-sm mb-4 text-center">{success}</div>
                )}

                <div className="w-full flex flex-col relative items-center justify-center">
                    <label className="block text-gray-600 font-medium mb-1">Hotel Identifiers</label>

                    <div className="flex w-full items-center justify-center space-x-2 mb-4">
                        <input
                            type="text"
                            value={hotelIdentifier}
                            onChange={(e) => setHotelIdentifier(e.target.value)}
                            placeholder="Enter hotel identifier"
                            className="w-1/2 border border-gray-300 rounded-md px-3 py-2
                                        focus:outline-none transition-all duration-100 ease-in-out transform
                                        rounded-lg focus:outline-none focus:border-[#32492D] focus:ring-1 focus:ring-[#32492D]"
                        />
                        <button
                            type="button"
                            onClick={addHotelIdentifier}
                            className="px-4 py-2 rounded-lg bg-[#32492D] text-white hover:bg-[#273823] transition-all duration-300"
                        >
                            Add
                        </button>
                    </div>

                    <div className="flex flex-wrap gap-2">
                        {hotelManagers.map((hm, index) => (
                            <div
                                key={index}
                                title={hm.status === "APPROVED" ? "Approved" : "Pending approval"}
                                className={`flex items-center gap-1 px-3 py-1 rounded bg-gray-200
                                ${hm.status === "APPROVED"
                                        ? "text-[#32492D]"
                                        : "text-yellow-600"
                                    }`}
                            >
                                <span>{hm.hotelIdentifier}</span>
                                {hotelManagers.length > 1 && (
                                    <button
                                        type="button"
                                        className="bg-red-600 text-white p-1 rounded-full"
                                        onClick={() => removeHotelIdentifier(hm.hotelIdentifier)}
                                    >
                                        <FaTimes size={10} />
                                    </button>
                                )}
                            </div>
                        ))}
                    </div>

                    <div className="mt-6 flex justify-end">
                        <button
                            type="button"
                            onClick={handleSave}
                            className="items-center justify-center px-4 py-2 text-white rounded-lg bg-[#32492D] hover:bg-[#273823] transition-all duration-300 ease-in-out transform"
                        >
                            Save Changes
                        </button>
                    </div>
                </div>
            </motion.div>

            {
                showPendingModal && (
                    <PopupModal
                        title="Awaiting Approval"
                        icon={<FaExclamationTriangle heckCircle className="text-[#32492D] mx-auto text-5xl mb-3" />}
                        message={`You've added ${pendingAdded.length
                            } hotel${pendingAdded.length > 1 ? "s" : ""} that need to be verified by an admin before you can create offers for it.`}
                        onClose={() => {
                            setShowPendingModal(false);
                            setPendingAdded([]);
                        }}
                    />
                )
            }
        </>
    );
};

export default ProfileHotelsSection;
