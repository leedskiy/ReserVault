import React, { useEffect, useState } from "react";
import { motion } from "framer-motion";
import { FaTimes, FaStar, FaBed, FaUserFriends } from "react-icons/fa";
import { FaLocationDot } from "react-icons/fa6";
import { useQuery } from "@tanstack/react-query";
import FacilityIcons from "../common/FacilityIcons";
import { Fancybox } from "@fancyapps/ui";
import "@fancyapps/ui/dist/fancybox/fancybox.css";
import { parse, eachDayOfInterval, isBefore, isAfter } from "date-fns";
import { useAuth } from "../../context/AuthContext";
import DateRangeSelector from "./DateRangeSelector";
import api from "../../api/axios";
import ReviewSection from "./ReviewSection";

const OfferDetailsModal = ({ offerId, onClose, onHotelClick, onBookingSuccess }) => {
    const { isUser } = useAuth();
    const formatter = "MM.dd.yyyy";

    const { data: offer, isLoading, error } = useQuery({
        queryKey: ["offer", offerId],
        queryFn: async () => {
            const { data } = await api.get(`/offers/${offerId}`);
            return data;
        },
        enabled: !!offerId,
    });

    const [bookingRange, setBookingRange] = useState({ startDate: null, endDate: null });
    const [bookedDateRanges, setBookedDateRanges] = useState([]);
    const [bookingError, setBookingError] = useState("");
    const [bookingSuccess, setBookingSuccess] = useState(false);

    useEffect(() => {
        Fancybox.bind("[data-fancybox='gallery']", {
            Thumbs: { autoStart: true },
        });
        return () => Fancybox.unbind("[data-fancybox='gallery']");
    }, [offer]);

    useEffect(() => {
        const fetchBookedDates = async () => {
            try {
                const res = await api.get(`/offers/${offerId}/booked-dates`);
                setBookedDateRanges(res.data);
            } catch (err) {
                console.error("Failed to load booked dates", err);
            }
        };

        if (offerId) fetchBookedDates();
    }, [offerId]);

    if (!offerId || !offer) return null;

    const offerStartDate = parse(offer.dateFrom, formatter, new Date());
    const offerEndDate = parse(offer.dateUntil, formatter, new Date());
    const offerRangeDates = eachDayOfInterval({ start: offerStartDate, end: offerEndDate });

    const bookedDates = bookedDateRanges.map(dateStr => {
        const d = new Date(dateStr);
        d.setHours(0, 0, 0, 0);
        return d;
    });

    const disabledDates = [];
    const calendarSpanStart = new Date(offerStartDate);
    calendarSpanStart.setDate(calendarSpanStart.getDate() - 30);
    const calendarSpanEnd = new Date(offerEndDate);
    calendarSpanEnd.setDate(calendarSpanEnd.getDate() + 30);

    for (let d = new Date(calendarSpanStart); d <= calendarSpanEnd; d.setDate(d.getDate() + 1)) {
        const date = new Date(d);
        const outOfRange = isBefore(date, offerStartDate) || isAfter(date, offerEndDate);
        const isBooked = bookedDates.some(b => b.getTime() === date.getTime());

        if (outOfRange || isBooked) {
            disabledDates.push(new Date(date));
        }
    }

    const limitText = (text, maxLength) =>
        text.length > maxLength ? text.substring(0, maxLength) + "..." : text;

    const getTotalPrice = () => {
        if (!bookingRange.startDate || !bookingRange.endDate) return 0;

        const start = parse(bookingRange.startDate, formatter, new Date());
        const end = parse(bookingRange.endDate, formatter, new Date());

        const nights = Math.max(
            Math.ceil((end.getTime() - start.getTime()) / (1000 * 60 * 60 * 24)) + 1,
            0
        );

        return nights * offer.pricePerNight;
    };

    return (
        <div className="fixed inset-0 flex items-center justify-center bg-black bg-opacity-50 z-50">
            <motion.div
                className="bg-white rounded-lg shadow-lg p-6 w-full max-w-5xl relative max-h-[90vh] flex flex-col"
                initial={{ opacity: 0, y: 50 }}
                animate={{ opacity: 1, y: 0 }}
                transition={{ duration: 0.4, ease: "easeOut" }}
            >
                <div>
                    <button
                        className="duration-200 ml-auto w-8 h-8 flex items-center justify-center rounded-lg text-[#32492D] hover:text-[#273823] hover:bg-gray-200"
                        onClick={onClose}
                    >
                        <FaTimes size={20} />
                    </button>
                </div>

                {isLoading ? (
                    <div className="text-center text-gray-600 my-8">Loading offer...</div>
                ) : error ? (
                    <div className="text-center text-red-500 my-8">Failed to load offer details.</div>
                ) : (
                    <div className="flex-1 overflow-y-auto">
                        <h2
                            className="text-2xl font-semibold text-[#32492D] text-center mb-2 hover:text-[#273823] transition-all cursor-pointer"
                            onClick={() => {
                                onClose();
                                onHotelClick?.(offer.hotelIdentifier);
                            }}
                        >
                            {offer.hotelName}
                        </h2>

                        <div className="flex items-center justify-center space-x-1 text-[#32492D] mb-4">
                            {Array.from({ length: offer.stars }).map((_, i) => (
                                <FaStar key={i} size={25} />
                            ))}
                        </div>

                        <div className="flex gap-10 p-4">
                            <div className="relative w-80 h-80 flex-shrink-0 rounded-md overflow-hidden group cursor-pointer">
                                <a href={offer.imagesUrls?.[0]} data-fancybox="gallery">
                                    <img
                                        src={offer.imagesUrls?.[0] ?? "https://via.placeholder.com/200"}
                                        alt={offer.title}
                                        className="w-full h-full object-cover transition duration-300 group-hover:brightness-75"
                                    />
                                    <div className="absolute inset-0 flex items-center justify-center opacity-0 group-hover:opacity-100 transition-opacity duration-300 bg-black bg-opacity-30 text-white text-lg font-semibold">
                                        Show more images
                                    </div>
                                </a>

                                {offer.imagesUrls?.slice(1).map((url, idx) => (
                                    <a key={idx} href={url} data-fancybox="gallery" className="hidden">
                                        <img src={url} alt={`Offer image ${idx + 2}`} />
                                    </a>
                                ))}
                            </div>

                            <div className="flex flex-col items-start justify-between">
                                <div className="flex items-start space-x-4">
                                    <h3 className="text-xl font-semibold text-gray-900">
                                        {offer.title}
                                    </h3>
                                    <div className="flex items-center bg-[#32492D] text-white rounded-lg px-4 py-1">
                                        {offer.rating}
                                    </div>
                                </div>

                                <div className="flex items-center text-[#32492D] mt-2">
                                    <FaLocationDot size={20} />
                                    <div className="ml-1 text-base text-gray-600">
                                        {offer.location.street}, {offer.location.postalCode} {offer.location.city}, {offer.location.country}
                                    </div>
                                </div>

                                {!isUser && (
                                    <div className="text-gray-600 text-base mt-2">
                                        {offer.dateFrom} → {offer.dateUntil}
                                    </div>
                                )}

                                {!isUser && (
                                    <div className="text-gray-600 text-base">
                                        {offer.pricePerNight} €/night
                                    </div>
                                )}

                                <div className="flex flex-wrap gap-2 items-center mt-2">
                                    <div className="flex items-center space-x-1 text-[#32492D]">
                                        <FaBed size={25} />
                                        <span>{offer.roomCount}</span>
                                    </div>
                                    <div className="flex items-center space-x-1 text-[#32492D]">
                                        <FaUserFriends size={25} />
                                        <span>{offer.peopleCount}</span>
                                    </div>
                                    <FacilityIcons facilities={offer.facilities} size={25} />
                                </div>

                                <div className="text-gray-600 mt-2">
                                    {limitText(offer.description, 470)}
                                </div>
                            </div>
                        </div>

                        {isUser && (
                            <div className="border-t flex flex-col items-center pt-10 mt-10 space-y-6">
                                <h3 className="text-xl font-semibold text-gray-900">Book this offer</h3>

                                <div className="flex flex-col w-3/4 space-y-6">
                                    <div className="flex space-x-20">
                                        <div className="w-1/2 space-y-2">
                                            <label className="block text-gray-600">Date Range:</label>

                                            <div>
                                                <DateRangeSelector
                                                    startDate={bookingRange.startDate}
                                                    endDate={bookingRange.endDate}
                                                    onChange={({ startDate, endDate }) =>
                                                        setBookingRange({ startDate, endDate })
                                                    }
                                                    hasError={!!bookingError}
                                                    disabledDates={disabledDates}
                                                    minDate={offerStartDate}
                                                    maxDate={offerEndDate}
                                                />
                                            </div>
                                        </div>

                                        {bookingError && (
                                            <p className="text-red-500 mt-2">{bookingError}</p>
                                        )}
                                        {bookingSuccess && (
                                            <p className="text-green-600 mt-2">Booking successful!</p>
                                        )}

                                        <div className="space-y-2">
                                            <div>
                                                <p className="text-gray-600">
                                                    Price Per Night: <span className="text-lg">{offer.pricePerNight} €</span>
                                                </p>
                                            </div>
                                            <div>
                                                <p className="text-gray-600">
                                                    Total Price: <span className="text-lg">{getTotalPrice()} €</span>
                                                </p>
                                            </div>
                                        </div>
                                    </div>

                                    <div className="flex items-center justify-center">
                                        <button
                                            className="mt-2 px-6 py-2 bg-[#32492D] text-white rounded-lg hover:bg-[#273823] transition-all w-60"
                                            onClick={async () => {
                                                if (!bookingRange.startDate || !bookingRange.endDate) {
                                                    setBookingError("Please select valid booking dates.");
                                                    return;
                                                }

                                                try {
                                                    const payload = {
                                                        offerId,
                                                        dateFrom: bookingRange.startDate,
                                                        dateUntil: bookingRange.endDate,
                                                    };

                                                    await api.post("/bookings", payload);
                                                    setBookingSuccess(true);
                                                    setBookingError("");
                                                    onClose();
                                                    onBookingSuccess();
                                                } catch (err) {
                                                    setBookingError("Booking failed. Try again.");
                                                    console.error(err);
                                                }
                                            }}
                                        >
                                            Submit
                                        </button>
                                    </div>
                                </div>
                            </div>
                        )}

                        <ReviewSection offerId={offer.id} />
                    </div>
                )}
            </motion.div>
        </div>
    );
};

export default OfferDetailsModal;
