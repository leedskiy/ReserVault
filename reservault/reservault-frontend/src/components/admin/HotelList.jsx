import { useMutation, useQueryClient } from "@tanstack/react-query";
import ItemCardList from "../common/ItemCardList";
import { FaStar } from "react-icons/fa";
import api from "../../api/axios";

const HotelList = ({ hotels, isLoading, error, onModify }) => {
    const queryClient = useQueryClient();

    const deleteMutation = useMutation({
        mutationFn: async (hotelId) => {
            await api.delete(`/admin/hotels/${hotelId}`);
        },
        onSuccess: () => {
            queryClient.invalidateQueries(["admin", "hotels"]);
        },
    });

    return (
        <ItemCardList
            items={hotels}
            isLoading={isLoading}
            error={error}
            getImage={(hotel) => hotel.imagesUrls?.[0] || "https://via.placeholder.com/200"}
            getTitle={(hotel) => (
                <h2 className="text-lg font-semibold text-gray-900">
                    {hotel.name}
                </h2>
            )}
            getSubtitle={(hotel) => `${hotel.location.city}, ${hotel.location.country}`}
            getDetails={(hotel) => (
                <div className="flex items-center space-x-1 text-[#32492D] mt-2">
                    {Array.from({ length: hotel.stars }).map((_, i) => (
                        <FaStar key={i} />
                    ))}
                </div>
            )}
            getDescription={(hotel) => hotel.description}
            onModify={onModify}
            onDelete={(id) => deleteMutation.mutate(id)}
            descriptionLimit={500}
            variant="admin"
        />
    );
};

export default HotelList;
