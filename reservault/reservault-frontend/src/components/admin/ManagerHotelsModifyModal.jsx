import { useState } from "react";
import { FaTimes } from "react-icons/fa";
import { motion } from "framer-motion";
import api from "../../api/axios";

const EditManagerHotelsModal = ({ managerId, currentHotelIdentifiers, onClose }) => {
    const [hotelIdentifiers, setHotelIdentifiers] = useState(currentHotelIdentifiers || []);
    const [hotelIdentifier, setHotelIdentifier] = useState("");
    const [error, setError] = useState("");

    const addHotelIdentifier = () => {
        if (hotelIdentifier && !hotelIdentifiers.includes(hotelIdentifier)) {
            setHotelIdentifiers([...hotelIdentifiers, hotelIdentifier]);
            setHotelIdentifier("");
        }
    };

    const removeHotelIdentifier = (identifier) => {
        setHotelIdentifiers(hotelIdentifiers.filter(id => id !== identifier));
    };

    const handleSave = async () => {
        try {
            await api.put(`/admin/managers/${managerId}/hotels`, hotelIdentifiers);
            onClose();
        } catch (error) {
            if (error.response && error.response.data) {
                setError(error.response.data);
            } else {
                setError("An unexpected error occurred.");
            }
        }
    };

    return (
        <div className="fixed inset-0 flex items-center justify-center bg-black bg-opacity-50 z-50">
            <motion.div
                className="bg-white p-6 rounded-lg shadow-lg w-full max-w-md"
                initial={{ opacity: 0, y: 50 }}
                animate={{ opacity: 1, y: 0 }}
                transition={{ duration: 0.3, ease: "easeOut" }}
            >
                <button
                    className="duration-200 ml-auto w-8 h-8 flex items-center justify-center rounded-lg text-[#32492D] hover:text-[#273823] hover:bg-gray-200"
                    onClick={onClose}
                >
                    <FaTimes size={20} />
                </button>

                <h2 className="text-2xl font-semibold text-center mb-4">Modify List of Hotels</h2>

                <div className="text-red-500 text-sm mb-4 text-center">
                    {error && <div>{error}</div>}
                </div>

                <div className="flex flex-col h-full p-4">
                    <label className="block text-gray-600">Hotel Identifiers</label>

                    <div className="flex gap-2">
                        <input
                            type="text"
                            value={hotelIdentifier}
                            onChange={(e) => setHotelIdentifier(e.target.value)}
                            placeholder="Enter hotel identifier"
                            className="w-full px-4 py-2 border rounded-lg focus:ring focus:ring-blue-300 transition-all duration-300"
                        />
                        <button
                            type="button"
                            onClick={addHotelIdentifier}
                            className="px-4 py-2 rounded-lg bg-[#32492D] text-white hover:bg-[#273823] transition-all duration-300"
                        >
                            Add
                        </button>
                    </div>

                    <div className="mt-4 flex flex-wrap gap-2">
                        {hotelIdentifiers.map((identifier, index) => (
                            <div key={index} className="flex items-center gap-1 bg-gray-200 px-2 py-1 rounded">
                                <span>{identifier}</span>
                                {hotelIdentifiers.length > 1 && (
                                    <button
                                        type="button"
                                        className="bg-red-600 text-white p-1 rounded-full"
                                        onClick={() => removeHotelIdentifier(identifier)}
                                    >
                                        <FaTimes size={10} />
                                    </button>
                                )}
                            </div>
                        ))}
                    </div>
                </div>

                <div className="mt-6 flex justify-end gap-4">
                    <button
                        type="button"
                        onClick={onClose}
                        className="px-8 py-2 min-w-32 border rounded-lg text-gray-700 hover:bg-gray-200 transition-all duration-300"
                    >
                        Cancel
                    </button>
                    <button
                        type="button"
                        onClick={handleSave}
                        className="px-8 py-2 min-w-32 text-white bg-[#32492D] rounded-lg hover:bg-[#273823] transition-all duration-300"
                    >
                        Submit
                    </button>
                </div>
            </motion.div>
        </div>
    );
};

export default EditManagerHotelsModal;
