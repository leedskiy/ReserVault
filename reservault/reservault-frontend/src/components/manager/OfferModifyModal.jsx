import { useState, useEffect } from "react";
import { useMutation, useQueryClient } from "@tanstack/react-query";
import { format } from "date-fns";
import api from "../../api/axios";
import FacilitiesSelector from "../common/FacilitiesSelector";
import ModifyFormContainer from "../common/ModifyFormContainer";
import DateRangeSelector from "../common/DateRangeSelector";
import { div } from "framer-motion/client";

const OfferModifyModal = ({ offer, onSubmit, onClose }) => {
    const [isDirty, setIsDirty] = useState(false);
    const [offerData, setOfferData] = useState({ ...offer, images: offer.images || [] });
    const [draggingIndex, setDraggingIndex] = useState(null);
    const queryClient = useQueryClient();
    const [isDragging, setIsDragging] = useState(false);
    const [newImages, setNewImages] = useState([]);
    const [imagesToDelete, setImagesToDelete] = useState([]);
    const [errorMessage, setErrorMessage] = useState("");

    useEffect(() => {
        const handleBeforeUnload = (event) => {
            if (isDirty) {
                event.preventDefault();
                event.returnValue = "";
            }
        };

        window.addEventListener("beforeunload", handleBeforeUnload);
        return () => {
            window.removeEventListener("beforeunload", handleBeforeUnload);
        };
    }, [isDirty]);

    const handleChange = (e) => {
        const { name, value, type } = e.target;
        setIsDirty(true);

        setOfferData((prev) => ({
            ...prev,
            [name]: type === "number" ? Number(value) : value,
        }));
    };

    const handleSubmit = async (e) => {
        e.preventDefault();

        if (!offerData.id) {
            return;
        }

        const from = new Date(offerData.dateFrom);
        const until = new Date(offerData.dateUntil);

        if (from > until) {
            setErrorMessage("Start date cannot be after end date.");
            return;
        }

        const formData = new FormData();

        formData.append(
            "offer",
            JSON.stringify({
                title: offerData.title,
                description: offerData.description,
                dateFrom: formatDateForBackend(offerData.dateFrom),
                dateUntil: formatDateForBackend(offerData.dateUntil),
                roomCount: offerData.roomCount,
                peopleCount: offerData.peopleCount,
                pricePerNight: offerData.pricePerNight,
                facilities: offerData.facilities,
                imagesUrls: offerData.images
            })
        );

        if (newImages.length > 0) {
            newImages.forEach((image) => {
                formData.append("images", image);
            });
        }

        if (imagesToDelete.length > 0) {
            for (const imageUrl of imagesToDelete) {
                await deleteImageMutation.mutateAsync(imageUrl);
            }
        }

        setIsDirty(false);
        onSubmit(offerData.id, formData);
    };

    const handleDragStart = (index) => {
        setDraggingIndex(index);
    };

    const handleDragOver = (event) => {
        event.preventDefault();
    };

    const formatDateForBackend = (dateStr) => {
        const date = new Date(dateStr);
        return format(date, "MM.dd.yyyy");
    };

    const handleDrop = (index) => {
        if (draggingIndex === null || draggingIndex === index) return;

        const updatedImages = [...offerData.images];
        const draggedImage = updatedImages.splice(draggingIndex, 1)[0];
        updatedImages.splice(index, 0, draggedImage);

        setOfferData((prev) => ({
            ...prev,
            images: updatedImages,
        }));

        setDraggingIndex(null);
        setIsDirty(true);
    };

    const handleRemoveImage = (index) => {
        const imageUrl = offerData.images[index];

        setImagesToDelete((prev) => [...prev, imageUrl]);

        setOfferData((prev) => ({
            ...prev,
            images: prev.images.filter((img, i) => i !== index),
        }));
    };

    const deleteImageMutation = useMutation({
        mutationFn: async (imageUrl) => {
            await api.delete(`/manager/offers/${offerData.id}/images`, {
                params: { imageUrl },
            });
        },
        onSuccess: (data, imageUrl) => {
            setOfferData((prev) => ({
                ...prev,
                images: prev.images.filter((img) => img !== imageUrl),
            }));

            queryClient.invalidateQueries(["admin", "offers"]);
        },
        onError: (error) => {
            console.error("Failed to delete image:", error.response?.data || error.message);
        },
    });

    const handleClose = () => {
        setImagesToDelete([]);
        onClose();
    };

    return (
        <ModifyFormContainer
            title="Modify Offer"
            onClose={handleClose}
            onSubmit={handleSubmit}
            images={offerData.images}
            handleDragStart={handleDragStart}
            handleDragOver={handleDragOver}
            handleDrop={handleDrop}
            handleRemoveImage={handleRemoveImage}
            newImages={newImages}
            setNewImages={setNewImages}
            isDragging={isDragging}
            setIsDragging={setIsDragging}
            isDirty={isDirty}
            setImagesToDelete={setImagesToDelete}
            itemsName="Offer"
            leftContent={(
                <div className="flex flex-col w-1/2 space-y-4">
                    {errorMessage && (
                        <div className="text-red-500 text-center">{errorMessage}</div>
                    )}

                    <div className="grid grid-cols-2 gap-4">
                        <div>
                            <label className="block text-gray-600">Hotel identifier</label>
                            <input
                                type="text"
                                className="w-full px-4 py-2 bg-gray-200 border rounded-lg"
                                value={offerData.hotelIdentifier}
                                disabled
                            />
                        </div>
                        <div>
                            <label className="block text-gray-600">Offer title</label>
                            <input
                                type="text"
                                name="title"
                                className="w-full px-4 py-2 border rounded-lg focus:ring focus:ring-blue-300"
                                onChange={handleChange}
                                value={offerData.title}
                                required
                            />
                        </div>
                    </div>

                    <div>
                        <label className="block text-gray-600">Description</label>
                        <textarea
                            name="description"
                            className="w-full px-4 py-2 border rounded-lg focus:ring focus:ring-blue-300"
                            onChange={handleChange}
                            value={offerData.description}
                            required
                        />
                    </div>

                    <div>
                        <label className="block text-gray-600">Date Range</label>

                        <DateRangeSelector
                            startDate={offerData.dateFrom}
                            endDate={offerData.dateUntil}
                            onChange={({ startDate, endDate }) => {
                                setIsDirty(true);
                                setOfferData((prev) => ({
                                    ...prev,
                                    dateFrom: startDate,
                                    dateUntil: endDate,
                                }));
                            }}
                            shadow={false}
                        />
                    </div>

                    <div className="grid grid-cols-3 gap-4">
                        <div>
                            <label className="block text-gray-600 mb-1">Room Count</label>
                            <div className="flex items-center gap-4">
                                <button
                                    type="button"
                                    onClick={() =>
                                        setOfferData((prev) => ({
                                            ...prev,
                                            roomCount: Math.max(1, prev.roomCount - 1),
                                        }))
                                    }
                                    className="px-2 py-1 bg-gray-200 rounded min-w-8"
                                >
                                    -
                                </button>
                                <span className="text-lg font-semibold">{offerData.roomCount}</span>
                                <button
                                    type="button"
                                    onClick={() =>
                                        setOfferData((prev) => ({
                                            ...prev,
                                            roomCount: prev.roomCount + 1,
                                        }))
                                    }
                                    className="px-2 py-1 bg-gray-200 rounded min-w-8"
                                >
                                    +
                                </button>
                            </div>
                        </div>

                        <div>
                            <label className="block text-gray-600 mb-1">People Count</label>
                            <div className="flex items-center gap-4">
                                <button
                                    type="button"
                                    onClick={() =>
                                        setOfferData((prev) => ({
                                            ...prev,
                                            peopleCount: Math.max(1, prev.peopleCount - 1),
                                        }))
                                    }
                                    className="px-2 py-1 bg-gray-200 rounded min-w-8"
                                >
                                    -
                                </button>
                                <span className="text-lg font-semibold">{offerData.peopleCount}</span>
                                <button
                                    type="button"
                                    onClick={() =>
                                        setOfferData((prev) => ({
                                            ...prev,
                                            peopleCount: prev.peopleCount + 1,
                                        }))
                                    }
                                    className="px-2 py-1 bg-gray-200 rounded min-w-8"
                                >
                                    +
                                </button>
                            </div>
                        </div>

                        <div>
                            <label className="block text-gray-600">Price Per Night (€)</label>
                            <input
                                type="number"
                                name="pricePerNight"
                                value={offerData.pricePerNight}
                                onChange={handleChange}
                                className="w-full px-4 py-2 border rounded-lg"
                                step="0.01"
                                min="0"
                                required
                            />
                        </div>
                    </div>
                </div>
            )}
            rightContent={(
                <div className="h-auto flex flex-col">
                    <label className="block text-gray-600">Facilities</label>

                    <FacilitiesSelector
                        facilities={offerData.facilities}
                        onToggle={(key) => {
                            setOfferData((prev) => ({
                                ...prev,
                                facilities: {
                                    ...prev.facilities,
                                    [key]: !prev.facilities?.[key],
                                },
                            }));
                            setIsDirty(true);
                        }}
                        layout="vertical"
                    />
                </div>
            )}
        />
    );


};

export default OfferModifyModal;