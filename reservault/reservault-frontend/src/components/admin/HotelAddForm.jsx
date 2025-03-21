import { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { motion } from "framer-motion";
import HotelStars from "./HotelStars";
import ImageUploader from "../common/ImageUploader";

const HotelAddForm = ({ onSubmit, onCancel }) => {
    const navigate = useNavigate();

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

        formData.append("hotel", JSON.stringify({
            identifier: hotelData.identifier,
            name: hotelData.name,
            description: hotelData.description,
            stars: hotelData.stars,
            location: hotelData.location,
        }));

        if (newImages.length > 0) {
            newImages.forEach((image) => {
                formData.append("images", image);
            });
        }

        setIsDirty(false);
        onSubmit(formData);
    };

    const handleCancel = () => {
        navigate("/admin/hotels/list");
    };

    return (
        <motion.div
            className="flex items-center justify-center flex flex-col"
            initial={{ opacity: 0, y: 50 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.4, ease: "easeOut" }}
        >
            <div className="flex flex-col items-center bg-white p-6 rounded-lg shadow-lg w-full">
                <h2 className="text-2xl font-semibold text-gray-900 text-center mb-4">
                    Add Hotel
                </h2>

                <form onSubmit={handleSubmit} className="space-y-4 w-3/4">
                    <div>
                        <label className="block text-gray-600">Identifier</label>
                        <input
                            type="text"
                            name="identifier"
                            className="w-full px-4 py-2 border rounded-lg focus:ring focus:ring-blue-300"
                            onChange={handleChange}
                            required
                        />
                    </div>

                    <div>
                        <label className="block text-gray-600">Hotel Name</label>
                        <input
                            type="text"
                            name="name"
                            className="w-full px-4 py-2 border rounded-lg focus:ring focus:ring-blue-300"
                            onChange={handleChange}
                            required
                        />
                    </div>

                    <div>
                        <label className="block text-gray-600">Description</label>
                        <textarea
                            name="description"
                            className="w-full px-4 py-2 border rounded-lg focus:ring focus:ring-blue-300"
                            onChange={handleChange}
                            required
                        />
                    </div>

                    <HotelStars hotelData={hotelData} setHotelData={setHotelData} direction="horizontal" />

                    <div className="grid grid-cols-2 gap-4">
                        <div>
                            <label className="block text-gray-600">Country</label>
                            <input
                                type="text"
                                name="country"
                                className="w-full px-4 py-2 border rounded-lg focus:ring focus:ring-blue-300"
                                onChange={handleChange}
                                required
                            />
                        </div>
                        <div>
                            <label className="block text-gray-600">City</label>
                            <input
                                type="text"
                                name="city"
                                className="w-full px-4 py-2 border rounded-lg focus:ring focus:ring-blue-300"
                                onChange={handleChange}
                                required
                            />
                        </div>
                        <div>
                            <label className="block text-gray-600">Street</label>
                            <input
                                type="text"
                                name="street"
                                className="w-full px-4 py-2 border rounded-lg focus:ring focus:ring-blue-300"
                                onChange={handleChange}
                                required
                            />
                        </div>
                        <div>
                            <label className="block text-gray-600">Postal Code</label>
                            <input
                                type="text"
                                name="postalCode"
                                className="w-full px-4 py-2 border rounded-lg focus:ring focus:ring-blue-300"
                                onChange={handleChange}
                                required
                            />
                        </div>
                    </div>

                    <ImageUploader
                        images={newImages}
                        setImages={setNewImages}
                        isDragging={isDragging}
                        setIsDragging={setIsDragging}
                    />

                    <button
                        type="submit"
                        className="w-full px-4 py-2 text-white rounded-lg bg-[#32492D] hover:bg-[#273823] transition-all duration-300 ease-in-out transform"
                    >
                        Submit
                    </button>

                    <button
                        type="button"
                        className="w-full px-4 py-2 text-gray-700 border rounded-lg hover:bg-gray-200 transition-all duration-300 ease-in-out transform"
                        onClick={handleCancel}
                    >
                        Cancel
                    </button>
                </form>
            </div>
        </motion.div>
    );
};

export default HotelAddForm;