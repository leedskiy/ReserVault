import { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import ImageUploader from "../common/ImageUploader";
import {
    FaWifi,
    FaParking,
    FaSwimmingPool,
    FaSnowflake,
    FaCoffee,
} from "react-icons/fa";
import { format } from "date-fns";
import api from "../../api/axios";
import AddFormContainer from "../common/AddFormContainer";
import TextInput from "../common/TextInput";
import FacilitiesSelector from "../common/FacilitiesSelector";
import DateRangeSelector from "../common/DateRangeSelector";

const OfferAddForm = ({ onSubmit }) => {
    const navigate = useNavigate();
    const [errorMessage, setErrorMessage] = useState("");

    const [offerData, setOfferData] = useState({
        hotelIdentifier: "",
        title: "",
        description: "",
        dateFrom: "",
        dateUntil: "",
        facilities: {
            wifi: false,
            parking: false,
            pool: false,
            airConditioning: false,
            breakfast: false,
        },
        roomCount: 1,
        peopleCount: 1,
        pricePerNight: ""
    });

    const [hotelIdentifiers, setHotelIdentifiers] = useState([]);
    const [newImages, setNewImages] = useState([]);
    const [isDragging, setIsDragging] = useState(false);
    const [isDirty, setIsDirty] = useState(false);

    useEffect(() => {
        const fetchHotels = async () => {
            try {
                const response = await api.get("/manager/hotels");
                const identifiers = response.data.map((hotel) => hotel.hotelIdentifier);
                setHotelIdentifiers(identifiers);
            } catch (error) {
                console.error("Failed to load hotel identifiers", error);
            }
        };

        fetchHotels();
    }, []);

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
        const { name, value } = e.target;
        setIsDirty(true);
        setOfferData((prev) => ({ ...prev, [name]: value }));
    };

    const handleFacilityToggle = (facilityKey) => {
        setIsDirty(true);
        setOfferData((prev) => ({
            ...prev,
            facilities: {
                ...prev.facilities,
                [facilityKey]: !prev.facilities[facilityKey],
            },
        }));
    };

    const formatDateForBackend = (dateStr) => {
        const date = new Date(dateStr);
        return format(date, "MM.dd.yyyy");
    };

    const handleSubmit = async (e) => {
        e.preventDefault();

        const from = new Date(offerData.dateFrom);
        const until = new Date(offerData.dateUntil);

        if (from > until) {
            setErrorMessage("Start date cannot be after end date.");
            return;
        }

        if (newImages.length === 0) {
            setErrorMessage("Please upload at least one image.");
            return;
        }

        setErrorMessage("");

        const formattedOffer = {
            ...offerData,
            dateFrom: formatDateForBackend(offerData.dateFrom),
            dateUntil: formatDateForBackend(offerData.dateUntil),
        };

        const formData = new FormData();
        formData.append("offer", JSON.stringify(formattedOffer));
        newImages.forEach((image) => formData.append("images", image));

        setIsDirty(false);
        onSubmit(formData);
    };


    const handleCancel = () => {
        navigate("/manager/offers/list");
    };

    const facilityIcons = [
        { key: "wifi", icon: FaWifi, label: "Wi-Fi" },
        { key: "parking", icon: FaParking, label: "Parking" },
        { key: "pool", icon: FaSwimmingPool, label: "Pool" },
        { key: "airConditioning", icon: FaSnowflake, label: "Air Conditioning" },
        { key: "breakfast", icon: FaCoffee, label: "Breakfast" },
    ];

    return (
        <AddFormContainer title="Add Offer" errorMessage={errorMessage} onSubmit={handleSubmit}>
            <div>
                <label className="block text-gray-600">Select Hotel</label>
                <select
                    name="hotelIdentifier"
                    value={offerData.hotelIdentifier}
                    onChange={handleChange}
                    className="w-full px-4 py-2 border rounded-lg focus:ring focus:ring-blue-300"
                    required
                >
                    <option value="">Select a hotel</option>
                    {hotelIdentifiers.map((id, idx) => (
                        <option key={idx} value={id}>{id}</option>
                    ))}
                </select>
            </div>

            <TextInput
                name="title"
                label="Offer Title"
                value={offerData.title}
                onChange={handleChange}
            />

            <div>
                <label className="block text-gray-600">Description</label>
                <textarea
                    name="description"
                    value={offerData.description}
                    onChange={handleChange}
                    className="w-full px-4 py-2 border rounded-lg focus:ring focus:ring-blue-300"
                    required
                />
            </div>

            <div>
                <label className="block text-gray-600 mb-1">Date Range</label>

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

            <FacilitiesSelector
                facilities={offerData.facilities}
                onToggle={handleFacilityToggle}
                layout="horizontal"
            />

            <div className="grid grid-cols-2 gap-4">
                <TextInput
                    name="roomCount"
                    label="Room Count"
                    type="number"
                    value={offerData.roomCount}
                    onChange={handleChange}
                />
                <TextInput
                    name="peopleCount"
                    label="People Count"
                    type="number"
                    value={offerData.peopleCount}
                    onChange={handleChange}
                />
            </div>

            <TextInput
                name="pricePerNight"
                label="Price Per Night (€)"
                type="number"
                step="0.01"
                value={offerData.pricePerNight}
                onChange={handleChange}
            />

            <ImageUploader
                images={newImages}
                setImages={setNewImages}
                isDragging={isDragging}
                setIsDragging={setIsDragging}
                itemsName="Offer"
            />

            <button
                type="submit"
                className="w-full px-4 py-2 text-white rounded-lg bg-[#32492D] hover:bg-[#273823] transition-all duration-300 ease-in-out transform"
            >
                Submit
            </button>
            <button
                type="button"
                onClick={handleCancel}
                className="w-full px-4 py-2 text-gray-700 border rounded-lg hover:bg-gray-200 transition-all duration-300 ease-in-out transform"
            >
                Cancel
            </button>
        </AddFormContainer>
    );
};

export default OfferAddForm;
