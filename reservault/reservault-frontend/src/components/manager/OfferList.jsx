import { useMutation, useQueryClient } from "@tanstack/react-query";
import { FaStar, FaBed, FaUserFriends } from "react-icons/fa";
import ItemCardList from "../common/ItemCardList";
import FacilityIcons from "../common/FacilityIcons";
import api from "../../api/axios";

const OfferList = ({ offers, isLoading, error, onModify }) => {
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
        <ItemCardList
            items={offers}
            isLoading={isLoading}
            error={error}
            getImage={(offer) => offer.imagesUrls?.[0] || "https://via.placeholder.com/200"}
            getTitle={(offer) => (
                <div className="flex items-center space-x-4">
                    <h2 className="text-lg font-semibold text-gray-900">{offer.title}</h2>
                    <div className="flex items-center justify-center text-l bg-[#32492D] text-white rounded-lg px-4">
                        {offer.rating}
                    </div>
                </div>
            )}
            getSubtitle={(offer) => (
                <div className="flex items-center space-x-4">
                    <p>{offer.hotelName}</p>
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
                    <p className="text-sm">{offer.dateFrom} - {offer.dateUntil}</p>
                </>
            )}
            getExtraIcons={(offer) => (
                <div className="flex flex-wrap gap-2 items-center text-sm">
                    <div className="flex items-center space-x-1 text-[#32492D]" title={`${offer.roomCount} room(s)`}>
                        <FaBed size={20} />
                        <span>{offer.roomCount}</span>
                    </div>
                    <div className="flex items-center space-x-1 text-[#32492D]" title={`${offer.peopleCount} people`}>
                        <FaUserFriends size={20} />
                        <span>{offer.peopleCount}</span>
                    </div>
                    <FacilityIcons facilities={offer.facilities} />
                </div>
            )}
            getPrice={(offer) => `${offer.pricePerNight} €`}
            getDescription={(offer) => offer.description}
            onModify={onModify}
            onDelete={(id) => deleteMutation.mutate(id)}
            contentWrapperClassName="space-y-2"
            descriptionLimit={200}
        />
    );
};

export default OfferList;
