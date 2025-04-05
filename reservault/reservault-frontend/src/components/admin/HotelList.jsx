import { useRef, useState } from "react";
import { useMutation, useQueryClient } from "@tanstack/react-query";
import ItemCardList from "../common/ItemCardList";
import { FaStar } from "react-icons/fa";
import api from "../../api/axios";
import ConfirmationPopup from "../common/ConfirmationPopup";
import Pagination from "../common/Pagination";

const HotelList = ({ hotels, isLoading, error, onModify }) => {
    const queryClient = useQueryClient();
    const [hotelToDelete, setHotelToDelete] = useState(null);

    const deleteMutation = useMutation({
        mutationFn: async (hotelId) => {
            await api.delete(`/admin/hotels/${hotelId}`);
        },
        onSuccess: () => {
            queryClient.invalidateQueries(["admin", "hotels"]);
        },
    });

    const handleDelete = (id) => {
        setHotelToDelete(id);
    };

    const confirmDelete = () => {
        deleteMutation.mutate(hotelToDelete, {
            onSuccess: () => {
                queryClient.invalidateQueries(["admin", "hotels"]);
                setHotelToDelete(null);
            },
        });
    };

    const listRef = useRef(null);
    const [currentPage, setCurrentPage] = useState(1);
    const itemsPerPage = 10;

    const totalPages = Math.ceil((hotels?.length || 0) / itemsPerPage);
    const paginatedHotels = hotels?.slice((currentPage - 1) * itemsPerPage, currentPage * itemsPerPage);

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
            <div ref={listRef}>
                <ItemCardList
                    items={paginatedHotels}
                    isLoading={isLoading}
                    error={error}
                    getImage={(hotel) => hotel.imagesUrls?.[0] || "https://via.placeholder.com/200"}
                    getTitle={(hotel) => (
                        <div className="flex items-center space-x-2">
                            <h2 className="text-lg font-semibold text-gray-900">
                                {hotel.name}
                            </h2>
                            <div className="text-sm text-gray-600">({hotel.identifier})</div>
                        </div>
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
                    onDelete={handleDelete}
                    descriptionLimit={500}
                    variant="admin"
                />
            </div>

            <Pagination
                currentPage={currentPage}
                totalPages={totalPages}
                onPageChange={handlePageChange}
            />

            <ConfirmationPopup
                isOpen={!!hotelToDelete}
                title="Please confirm action"
                message={"Are you sure you want to delete this hotel? Action cannot be undone, and all associated " +
                    "data will be lost. It will delete associated offers, hotemanagaers, bookings, payments etc."}
                onConfirm={confirmDelete}
                onCancel={() => setHotelToDelete(null)}
                confirmLabel="Delete"
                cancelLabel="Cancel"
            />
        </>
    );
};

export default HotelList;
