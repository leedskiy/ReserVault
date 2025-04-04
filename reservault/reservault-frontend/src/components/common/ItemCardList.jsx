import { motion } from "framer-motion";
import { FaEdit, FaTrash } from "react-icons/fa";
import { FaCommentAlt } from "react-icons/fa";
import DropdownButton from "../common/DropdownButton";

const ItemCardList = ({
    items,
    isLoading,
    error,
    getImage,
    getTitle,
    getSubtitle,
    getDetails,
    getDescription,
    getPrice,
    getExtraIcons,
    onModify,
    onDelete,
    contentWrapperClassName = "",
    descriptionLimit = 200,
    onCardClick,
    variant,
    hotelFilter = false,
}) => {
    const limitText = (text, maxLength) =>
        text.length > maxLength ? text.substring(0, maxLength) + "..." : text;

    return (
        <>
            {isLoading && <div className="text-center mb-6 text-gray-600">Loading...</div>}

            {error && <div className="text-center mb-6 text-red-500">Failed to load data.</div>}

            {!isLoading && !error && items?.length === 0 && (
                <div className="text-center mb-6 text-gray-600">
                    {variant === "manager" ? "Start by creating an Offer" : ""}
                    {variant === "admin" ? "Start by creating a Hotel" : ""}
                    {variant === "user" ? "No offers found, try using other filters" : ""}
                </div>
            )}

            {items?.length > 0 &&
                (
                    <motion.div className="space-y-6 flex flex-col items-center w-full">
                        {items?.map((item, index) => (
                            <motion.div
                                key={item.id}
                                className="bg-white shadow-md rounded-lg flex border border-gray-200 p-4 min-w-full relative"
                                initial={{ opacity: 0, y: 30 }}
                                animate={{ opacity: 1, y: 0 }}
                                transition={{ delay: index * 0.1, duration: 0.4, ease: "easeOut" }}
                                onClick={() => onCardClick?.(item)}
                            >
                                <div className="w-64 h-64 flex-shrink-0 rounded-md overflow-hidden">
                                    <img
                                        src={getImage(item)}
                                        alt={typeof getTitle(item) === "string" ? getTitle(item) : ""}
                                        className="w-full h-full object-cover"
                                    />
                                </div>

                                <div className={`w-full px-6 flex flex-col ${contentWrapperClassName}`}>
                                    {getTitle && <div>{getTitle(item)}</div>}
                                    {getSubtitle && !hotelFilter && <div className="text-sm text-gray-600">{getSubtitle(item)}</div>}
                                    {getDetails && !hotelFilter && <div className="text-sm text-gray-600 space-y-2">{getDetails(item)}</div>}
                                    {variant !== "user" && getExtraIcons && (
                                        <div className="text-sm text-gray-600 flex flex-wrap gap-2">
                                            {getExtraIcons(item)}
                                        </div>
                                    )}
                                    {variant !== "user" && getPrice && <p className="text-sm text-gray-600">{getPrice(item)}</p>}
                                    {getDescription && (
                                        <p className="text-sm text-gray-600 mt-4">
                                            {limitText(getDescription(item), descriptionLimit)}
                                        </p>
                                    )}
                                </div>

                                <div className="flex flex-col justify-between items-end text-right">
                                    {variant === "manager" || variant === "admin" ? (
                                        <DropdownButton
                                            itemId={item.id}
                                            menuItems={[
                                                {
                                                    label: "Modify",
                                                    icon: FaEdit,
                                                    onClick: () => onModify(item),
                                                },
                                                {
                                                    label: "Delete",
                                                    icon: FaTrash,
                                                    onClick: () => onDelete(item.id),
                                                },
                                            ]}
                                        />
                                    ) : (
                                        <div className="flex flex-col items-end space-y-3">
                                            <div className="flex items-center space-x-2">
                                                <div className="flex items-center space-x-1 text-[#32492D]" title={`${item.reviews.length} review(s)`}>
                                                    <FaCommentAlt size={15} />
                                                    <span>{item.reviews.length}</span>
                                                </div>
                                                <div className="flex items-center justify-center text-l bg-[#32492D] text-white rounded-lg px-3 max-w-16 py-1 w-10">
                                                    {item.rating}
                                                </div>
                                            </div>

                                            <div className="text-lg text-gray-600">{getPrice(item)}</div>

                                            <div className="flex flex-col text-sm gap-2 mt-auto">
                                                {getExtraIcons && getExtraIcons(item)}
                                            </div>
                                        </div>
                                    )}
                                </div>
                            </motion.div>
                        ))}
                    </motion.div>
                )
            }
        </>
    );
};

export default ItemCardList;
