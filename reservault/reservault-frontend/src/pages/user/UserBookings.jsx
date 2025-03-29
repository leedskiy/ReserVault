import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../../context/AuthContext";
import { motion } from "framer-motion";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { FaCreditCard, FaTimesCircle, FaCheckCircle } from "react-icons/fa";
import Header from "../../components/common/Header";
import api from "../../api/axios";
import DropdownButton from "../../components/common/DropdownButton";
import OfferDetailsModal from "../../components/common/OfferDetailsModal";
import HotelDetailsModal from "../../components/common/HotelDetailsModal";
import PopupModal from "../../components/common/PopupModal";

const UserBookings = () => {
    const { isAuthenticated, isUser, loading } = useAuth();
    const navigate = useNavigate();
    const queryClient = useQueryClient();
    const [error, setError] = useState("");

    const [selectedOfferId, setSelectedOfferId] = useState(null);
    const [selectedHotelIdentifier, setSelectedHotelIdentifier] = useState(null);
    const [now, setNow] = useState(new Date());
    const [showPaymentModal, setShowPaymentModal] = useState(false);

    useEffect(() => {
        const interval = setInterval(() => {
            setNow(new Date());
        }, 60000); // 60sec

        return () => clearInterval(interval);
    }, []);

    useEffect(() => {
        if (!loading && (!isAuthenticated || !isUser)) {
            navigate("/login");
        }
    }, [loading, isAuthenticated, isUser, navigate]);

    const { data: bookings, isLoading } = useQuery({
        queryKey: ["user", "bookings"],
        queryFn: async () => {
            const { data } = await api.get("/bookings");
            return data;
        },
        retry: false
    });

    const simulatePaymentMutation = useMutation({
        mutationFn: async (bookingId) => {
            const { data } = await api.post(`/bookings/${bookingId}/pay`);
            return data;
        },
        onSuccess: () => {
            queryClient.invalidateQueries(["user", "bookings"]);
        }
    });

    const limitText = (text, maxLength) =>
        text.length > maxLength ? text.substring(0, maxLength) + "..." : text;

    const cancelBookingMutation = useMutation({
        mutationFn: async (bookingId) => {
            await api.delete(`/bookings/${bookingId}`);
        },
        onSuccess: () => {
            queryClient.invalidateQueries(["user", "bookings"]);
        }
    });

    const handlePay = async (bookingId) => {
        await simulatePaymentMutation.mutateAsync(bookingId);
        setShowPaymentModal(true);
    };

    const handleCancel = (bookingId) => cancelBookingMutation.mutate(bookingId);

    const getRemainingTime = (expiresAt) => {
        const diff = new Date(expiresAt) - now;
        if (diff <= 0) return "Expired";

        const minutes = Math.ceil(diff / 1000 / 60); // min

        return `${minutes} min${minutes > 1 ? "s" : ""}`;
    };

    return (
        <>
            <Header />

            <div className="container mx-auto max-w-6xl py-8">
                <h1 className="text-3xl font-bold text-gray-900 mb-6 text-center">Bookings</h1>

                {isLoading && <p className="text-center mb-6 text-gray-600">Loading bookings...</p>}
                {error && <p className="text-center text-red-500">{error}</p>}
                {!isLoading && bookings?.length === 0 && (
                    <p className="text-center text-gray-600">You have no bookings yet.</p>
                )}

                <motion.div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
                    {bookings?.map((booking, index) => (
                        <motion.div
                            key={booking.id}
                            className="bg-white shadow-md rounded-lg p-4 w-full border border-gray-200 flex flex-col"
                            initial={{ opacity: 0, y: 30 }}
                            animate={{ opacity: 1, y: 0 }}
                            transition={{ delay: index * 0.1, duration: 0.4, ease: "easeOut" }}
                        >
                            <div className="flex">
                                <div className="flex flex-col w-full space-y-2">
                                    <div className="flex items-center space-x-2">
                                        <button
                                            onClick={() => setSelectedOfferId(booking.offerId)}
                                            className="text-left text-lg font-semibold text-[#32492D] hover:text-[#273823] transition-all duration-200 ease-in-out transform cursor-pointer flex items-center gap-2"
                                        >
                                            {limitText(booking.offerTitle, 30)}
                                        </button>

                                        {booking.paymentStatus === "PAID" && (
                                            <FaCheckCircle className="text-[#32492D]" title="Booking completed" />
                                        )}
                                    </div>


                                    <button
                                        onClick={() => setSelectedHotelIdentifier(booking.hotelIdentifier)}
                                        className="text-left text-base font-bold text-[#32492D] hover:text-[#273823] transition-all duration-200 ease-in-out transform cursor-pointer"
                                    >
                                        {booking.hotelName}
                                    </button>

                                    <div className="text-sm text-gray-600">
                                        <p>{booking.hotelLocation?.city}, {booking.hotelLocation?.country}</p>
                                    </div>

                                    <div className="text-sm text-gray-600 space-y-2">
                                        <p>{booking.dateFrom} → {booking.dateUntil}</p>
                                    </div>

                                    <div className="flex space-x-2">
                                        <p className="text-lg text-gray-600">{booking.price} €</p>


                                        {booking.paymentStatus === "PENDING" && (
                                            <div>
                                                <span className="text-sm text-red-500">
                                                    ({getRemainingTime(booking.expiresAt)} to pay)
                                                </span>
                                            </div>
                                        )}
                                    </div>
                                </div>

                                <div className="ml-auto">
                                    {booking.paymentStatus === "PENDING" && (
                                        <div className="ml-auto">
                                            <DropdownButton
                                                itemId={booking.id}
                                                menuItems={[
                                                    {
                                                        label: "Pay",
                                                        icon: FaCreditCard,
                                                        onClick: () => handlePay(booking.id),
                                                    },
                                                    {
                                                        label: "Cancel",
                                                        icon: FaTimesCircle,
                                                        onClick: () => handleCancel(booking.id),
                                                    }
                                                ]}
                                            />
                                        </div>
                                    )}
                                </div>
                            </div>
                        </motion.div>
                    ))}
                </motion.div>
            </div>

            {selectedOfferId && (
                <OfferDetailsModal
                    offerId={selectedOfferId}
                    onClose={() => setSelectedOfferId(null)}
                    onHotelClick={(hotelIdentifier) => {
                        setSelectedOfferId(null);
                        setSelectedHotelIdentifier(hotelIdentifier);
                    }}
                />
            )}

            {selectedHotelIdentifier && (
                <HotelDetailsModal
                    hotelIdentifier={selectedHotelIdentifier}
                    onClose={() => setSelectedHotelIdentifier(null)}
                />
            )}

            {showPaymentModal && (
                <PopupModal
                    message="We've sent payment instructions to your email. Please follow the steps to complete the payment."
                    onClose={() => setShowPaymentModal(false)}
                />
            )}
        </>
    );
};

export default UserBookings;
