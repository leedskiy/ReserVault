import { useState } from "react";
import { useMutation, useQueryClient } from "@tanstack/react-query";
import { FaStar, FaBed, FaUserFriends, FaCheckCircle, FaCommentAlt } from "react-icons/fa";
import ItemCardList from "./ItemCardList";
import FacilityIcons from "./FacilityIcons";
import api from "../../api/axios";
import OfferDetailsModal from "./OfferDetailsModal";
import HotelDetailsModal from "./HotelDetailsModal";
import PopupModal from "../common/PopupModal";

const OfferList = ({ offers, isLoading, error, onModify, variant = "manager" }) => {
    const [selectedOfferDetails, setSelectedOfferDetails] = useState(null);
    const [selectedHotel, setSelectedHotel] = useState(null);
    const [hotelError, setHotelError] = useState(null);
    const [hotelLoading, setHotelLoading] = useState(false);
    const [showReminder, setShowReminder] = useState(false);

    const fetchHotelDetails = async (hotelIdentifier) => {
        try {
            setHotelLoading(true);
            const { data } = await api.get(`/hotels/${hotelIdentifier}`);
            setSelectedHotel(data);
        } catch (error) {
            console.error("Failed to fetch hotel details:", error);
            setHotelError("Failed to fetch hotel details.");
        } finally {
            setHotelLoading(false);
        }
    };

    const queryClient = useQueryClient();

    const deleteMutation = useMutation({
        mutationFn: async (offerId) => {
            await api.delete(`/manager/offers/${offerId}`);
        },
        onSuccess: () => {
            queryClient.invalidateQueries(["manager", "offers"]);
        },
    });

    return (
        <>
            <ItemCardList
                items={offers}
                variant={variant}
                isLoading={isLoading}
                error={error}
                getImage={(offer) => offer.imagesUrls?.[0] || "https://via.placeholder.com/200"}
                getTitle={(offer) => (
                    <div className="flex items-center space-x-4">
                        <h2 className="text-lg font-semibold text-[#32492D] hover:text-[#273823] transition-all duration-200 ease-in-out transform cursor-pointer">
                            {offer.title}
                        </h2>
                        {variant === "manager" && (
                            <div className="flex items-center space-x-2">
                                <div className="flex items-center space-x-1 text-[#32492D]" title={`${offer.reviews.length} review(s)`}>
                                    <FaCommentAlt size={15} />
                                    <span>{offer.reviews.length}</span>
                                </div>
                                <div className="flex items-center justify-center text-l bg-[#32492D] text-white rounded-lg max-w-16 py-1 w-10">
                                    {offer.rating}
                                </div>
                            </div>
                        )}
                    </div>
                )}

                getSubtitle={(offer) => (
                    <div className="flex items-center space-x-4">
                        <button
                            onClick={(e) => {
                                e.stopPropagation();
                                fetchHotelDetails(offer.hotelIdentifier);
                            }}
                            className="text-base font-bold text-[#32492D] hover:text-[#273823] transition-all duration-200 ease-in-out transform cursor-pointer"
                        >
                            {offer.hotelName}
                        </button>
                        <div className="flex items-center space-x-1 text-[#32492D]">
                            {Array.from({ length: offer.stars }).map((_, i) => (
                                <FaStar key={i} />
                            ))}
                        </div>
                    </div>
                )}

                getDetails={(offer) => (
                    <>
                        <p className="text-sm">{offer.location.city}, {offer.location.country}</p>
                        {variant === "manager" && (
                            <p className="text-sm">{offer.dateFrom} → {offer.dateUntil}</p>
                        )}
                    </>
                )}
                getExtraIcons={(offer) => {
                    const isUser = variant === "user";

                    return (
                        <div
                            className={`text-sm text-[#32492D] ${isUser ? "flex flex-col items-end gap-9" : "flex flex-wrap gap-2 items-center"
                                }`}
                        >
                            <div className="flex gap-2 flex-grow">
                                <div className="flex items-center space-x-1" title={`${offer.roomCount} room(s)`}>
                                    <FaBed size={20} />
                                    <span>{offer.roomCount}</span>
                                </div>
                                <div className="flex items-center space-x-1" title={`${offer.peopleCount} people`}>
                                    <FaUserFriends size={20} />
                                    <span>{offer.peopleCount}</span>
                                </div>
                            </div>

                            <div className="flex flex-col mt-auto justify-end">
                                <FacilityIcons
                                    facilities={offer.facilities}
                                    direction={isUser ? "column" : "row"}
                                />
                            </div>
                        </div>
                    );
                }}
                getPrice={(offer) => `${offer.pricePerNight} €`}
                getDescription={(offer) => offer.description}
                onModify={onModify}
                onDelete={(id) => deleteMutation.mutate(id)}
                contentWrapperClassName="space-y-2"
                descriptionLimit={(variant === "user" ? 520 : 200)}
                onCardClick={(offer) => setSelectedOfferDetails(offer)}
            />

            {selectedOfferDetails && (
                <OfferDetailsModal
                    offerId={selectedOfferDetails.id}
                    onClose={() => setSelectedOfferDetails(null)}
                    onHotelClick={(identifier) => fetchHotelDetails(identifier)}
                    onBookingSuccess={() => setShowReminder(true)}
                />
            )}

            {selectedHotel && (
                <HotelDetailsModal
                    hotelIdentifier={selectedHotel.identifier}
                    onClose={() => setSelectedHotel(null)}
                />
            )}

            {showReminder && (
                <PopupModal
                    title="Booking Confirmed"
                    icon={<FaCheckCircle className="text-[#32492D] mx-auto text-5xl mb-3" />}
                    message="To complete your booking, go to the Bookings page and proceed with payment. You have 60 minutes before it expires."
                    onClose={() => setShowReminder(false)}
                />
            )}
        </>
    );
};

export default OfferList;
