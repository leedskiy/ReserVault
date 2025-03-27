import { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import HotelStars from "../common/HotelStars";
import ImageUploader from "../common/ImageUploader";
import AddFormContainer from "../common/AddFormContainer";
import TextInput from "../common/TextInput";

const HotelAddForm = ({ onSubmit }) => {
    const navigate = useNavigate();
    const [errorMessage, setErrorMessage] = useState("");

    const [hotelData, setHotelData] = useState({
        identifier: "",
        name: "",
        description: "",
        stars: 5,
        location: { country: "", city: "", street: "", postalCode: "" },
        images: [],
    });

    const [newImages, setNewImages] = useState([]);
    const [isDragging, setIsDragging] = useState(false);
    const [isDirty, setIsDirty] = useState(false);

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

        if (["country", "city", "street", "postalCode"].includes(name)) {
            setHotelData((prev) => ({
                ...prev,
                location: { ...prev.location, [name]: value },
            }));
        } else {
            setHotelData((prev) => ({ ...prev, [name]: value }));
        }
    };

    const handleSubmit = async (e) => {
        e.preventDefault();

        const formData = new FormData();

        if (newImages.length === 0) {
            setErrorMessage("Please upload at least one image.");
            return;
        }

        formData.append("hotel", JSON.stringify({
            identifier: hotelData.identifier,
            name: hotelData.name,
            description: hotelData.description,
            stars: hotelData.stars,
            location: hotelData.location,
        }));

        newImages.forEach((image) => formData.append("images", image));

        setIsDirty(false);
        onSubmit(formData);
    };

    const handleCancel = () => {
        navigate("/admin/hotels/list");
    };

    return (
        <AddFormContainer
            title="Add Hotel"
            errorMessage={errorMessage}
            onSubmit={handleSubmit}
        >
            <TextInput
                name="identifier"
                label="Identifier"
                value={hotelData.identifier}
                onChange={handleChange}
            />

            <TextInput
                name="name"
                label="Hotel Name"
                value={hotelData.name}
                onChange={handleChange}
            />

            <div>
                <label className="block text-gray-600">Description</label>
                <textarea
                    name="description"
                    value={hotelData.description}
                    onChange={handleChange}
                    className="w-full px-4 py-2 border rounded-lg focus:ring focus:ring-blue-300"
                    required
                />
            </div>

            <div>
                <label className="block text-gray-600">Stars</label>
                <HotelStars
                    hotelData={hotelData}
                    setHotelData={setHotelData}
                    direction="horizontal"
                />
            </div>

            <div className="grid grid-cols-2 gap-4">
                <TextInput
                    name="country"
                    label="Country"
                    value={hotelData.location.country}
                    onChange={handleChange}
                />
                <TextInput
                    name="city"
                    label="City"
                    value={hotelData.location.city}
                    onChange={handleChange}
                />
                <TextInput
                    name="street"
                    label="Street"
                    value={hotelData.location.street}
                    onChange={handleChange}
                />
                <TextInput
                    name="postalCode"
                    label="Postal Code"
                    value={hotelData.location.postalCode}
                    onChange={handleChange}
                />
            </div>

            <ImageUploader
                images={newImages}
                setImages={setNewImages}
                isDragging={isDragging}
                setIsDragging={setIsDragging}
                itemsName="Hotel"
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
        </AddFormContainer >
    );

};

export default HotelAddForm;