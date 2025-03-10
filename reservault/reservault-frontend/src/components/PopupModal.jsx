import { FaEnvelope } from "react-icons/fa";

const PopupModal = ({ message, onClose }) => {
    return (
        <div className="fixed inset-0 flex items-center justify-center bg-black bg-opacity-50 z-50">
            <div className="bg-white p-6 rounded-lg shadow-lg w-full max-w-sm text-center">
                <FaEnvelope className="text-[#32492D] mx-auto text-5xl mb-3" />
                <h2 className="text-lg font-semibold text-gray-900">Check Your Email</h2>
                <p className="text-gray-600 mt-2">{message}</p>

                <button
                    onClick={onClose}
                    className="mt-4 px-6 py-2 bg-gray-900 text-white rounded-lg hover:bg-[#32492D] transition-all duration-300 ease-in-out transform"
                >
                    OK
                </button>
            </div>
        </div>
    );
};

export default PopupModal;