import { FaArrowLeft, FaArrowRight } from "react-icons/fa";

const Pagination = ({ currentPage, totalPages, onPageChange }) => {
    if (totalPages <= 1) return null;

    return (
        <div className="flex justify-center items-center mt-14 mb-6 space-x-6">
            <button
                onClick={() => onPageChange(currentPage - 1)}
                disabled={currentPage === 1}
                className="px-3 py-2 rounded-lg disabled:opacity-50 disabled:shadow-sm disabled:hover:bg-white 
                hover:bg-gray-200 bg-white text-gray-900 text-white transition-all duration-200 ease-in-out transform shadow-md"
            >
                <FaArrowLeft className="text-black" />
            </button>
            <div className="flex justify-center items-center space-x-2">
                {Array.from({ length: totalPages }).map((_, i) => (
                    <button
                        key={i}
                        onClick={() => onPageChange(i + 1)}
                        className={`w-10 h-10 rounded-lg font-bold  transition-all duration-200 ease-in-out transform bg-white
                            ${currentPage === i + 1 ? "bg-gray-300 shadow-sm text-gray-600 cursor-default" : "text-gray-900 shadow-md hover:bg-gray-200"}`}
                    >
                        {i + 1}
                    </button>
                ))}
            </div>
            <button
                onClick={() => onPageChange(currentPage + 1)}
                disabled={currentPage === totalPages}
                className="px-3 py-2 rounded-lg disabled:opacity-50 disabled:shadow-sm disabled:hover:bg-white 
                hover:bg-gray-200 bg-white text-gray-900 text-white transition-all duration-200 ease-in-out transform shadow-md"
            >
                <FaArrowRight className="text-black" />
            </button>
        </div>
    );
};

export default Pagination;
