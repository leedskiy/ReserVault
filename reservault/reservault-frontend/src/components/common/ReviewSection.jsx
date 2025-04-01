import { useState } from "react";
import { useAuth } from "../../context/AuthContext";
import { FaTrash, FaCommentAlt } from "react-icons/fa";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { useForm } from "react-hook-form";
import api from "../../api/axios";
import DropdownButton from "../common/DropdownButton";

const ReviewSection = ({ offerId }) => {
    const { user, isUser, isManager } = useAuth();
    const queryClient = useQueryClient();

    const [expandedReviewIds, setExpandedReviewIds] = useState([]);
    const [isAdding, setIsAdding] = useState(false);
    const {
        register,
        handleSubmit,
        reset,
        formState: { errors },
    } = useForm();
    const [respondingToId, setRespondingToId] = useState(null);
    const {
        register: registerResponse,
        handleSubmit: handleSubmitResponse,
        setError: setResponseError,
        formState: { errors: responseErrors },
        reset: resetResponse,
    } = useForm();

    const { data: reviews = [], isLoading } = useQuery({
        queryKey: ["reviews", offerId],
        queryFn: async () => {
            const { data } = await api.get(`/offers/${offerId}/reviews`);
            return data;
        },
    });
    const userAlreadyReviewed = reviews.some((r) => r.userId === user?.id);

    const addReviewMutation = useMutation({
        mutationFn: async (reviewData) => {
            const { data } = await api.post(`/offers/${offerId}/reviews`, reviewData);
            return data;
        },
        onSuccess: () => {
            queryClient.invalidateQueries(["reviews", offerId]);
            reset();
            setIsAdding(false);
        },
    });

    const deleteReview = useMutation({
        mutationFn: async (reviewId) => {
            await api.delete(`/offers/${offerId}/reviews/${reviewId}`);
        },
        onSuccess: () => {
            queryClient.invalidateQueries(["reviews", offerId]);
        },
    });

    const toggleExpand = (id) => {
        setExpandedReviewIds((prev) =>
            prev.includes(id) ? prev.filter((e) => e !== id) : [...prev, id]
        );
    };

    const onSubmit = ({ title, comment, rating }) => {
        const parsedRating = parseFloat(rating);
        addReviewMutation.mutate({ title, comment, rating: parsedRating });
    };

    const respondToReview = useMutation({
        mutationFn: async ({ reviewId, comment }) => {
            return await api.post(`/manager/reviews/${reviewId}/response`, { comment });
        },
        onSuccess: () => {
            setRespondingToId(null);
            resetResponse();
            queryClient.invalidateQueries(["reviews", offerId]);
        }
    });

    const deleteResponse = useMutation({
        mutationFn: async (reviewId) => {
            return await api.delete(`/manager/reviews/${reviewId}/response`);
        },
        onSuccess: () => {
            queryClient.invalidateQueries(["reviews", offerId]);
        },
    });

    const limitText = (text, maxLength) =>
        text.length > maxLength ? text.substring(0, maxLength) + "..." : text;

    return (
        <div className="w-full bg-white border-t pt-10 mt-10 space-y-6 mb-20">
            <div className="flex items-center justify-center space-x-2">
                <h3 className="text-xl font-semibold text-gray-900 text-center">Reviews</h3>

                <div className="flex items-center space-x-1 text-[#32492D]" title={`${reviews.length} review(s)`}>
                    <FaCommentAlt size={15} />
                    <span>{reviews.length}</span>
                </div>
            </div>

            <div className="max-h-[500px] overflow-y-auto px-4">
                {isLoading ? (
                    <div className="text-center text-gray-600 my-8">Loading reviews...</div>
                ) : reviews.length > 0 || isAdding ? (
                    <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6 border p-4 rounded-md bg-gray-200 bg-opacity-60">
                        {isUser && isAdding && (
                            <form
                                onSubmit={handleSubmit(onSubmit)}
                                className="border rounded-md p-4 bg-white flex flex-col min-h-[250px] shadow-lg"
                            >
                                <input
                                    type="text"
                                    placeholder="Review title"
                                    {...register("title", { required: "Title is required" })}
                                    className="w-full border px-3 py-2 rounded-md mb-1 
                                        focus:outline-none transition-all duration-100 ease-in-out transform
                                        rounded-lg focus:outline-none focus:border-[#32492D] focus:ring-1 focus:ring-[#32492D]"
                                />
                                {errors.title && <p className="text-red-500 text-sm">{errors.title.message}</p>}

                                <input
                                    type="number"
                                    step="0.1"
                                    min="1"
                                    max="10"
                                    placeholder="Rating (1-10)"
                                    {...register("rating", {
                                        required: "Rating is required",
                                        min: { value: 1, message: "Minimum rating is 1" },
                                        max: { value: 10, message: "Maximum rating is 10" },
                                    })}
                                    className="w-full border px-3 py-2 rounded-md mb-1
                                        focus:outline-none transition-all duration-100 ease-in-out transform
                                        rounded-lg focus:outline-none focus:border-[#32492D] focus:ring-1 focus:ring-[#32492D]"
                                />
                                {errors.rating && <p className="text-red-500 text-sm">{errors.rating.message}</p>}

                                <textarea
                                    placeholder="Write your review..."
                                    {...register("comment", { required: "Comment is required" })}
                                    rows={3}
                                    className="w-full border px-3 py-2 rounded-md mb-1 focus:outline-none
                                        focus:outline-none transition-all duration-100 ease-in-out transform
                                        rounded-lg focus:outline-none focus:border-[#32492D] focus:ring-1 focus:ring-[#32492D]"
                                />
                                {errors.comment && <p className="text-red-500 text-sm">{errors.comment.message}</p>}

                                <div className="flex justify-end gap-2 mt-auto">
                                    <button
                                        type="button"
                                        onClick={() => {
                                            setIsAdding(false);
                                            reset();
                                        }}
                                        className="px-4 py-2 min-w-24 border rounded-lg text-gray-700 hover:bg-gray-200 transition-all duration-300"
                                    >
                                        Cancel
                                    </button>
                                    <button
                                        type="submit"
                                        className="px-4 py-2 min-w-24 text-white bg-[#32492D] rounded-lg hover:bg-[#273823] transition-all duration-300"
                                    >
                                        Submit
                                    </button>
                                </div>
                            </form>
                        )}

                        {[...reviews].reverse().map((review) => {
                            const isExpanded = expandedReviewIds.includes(review.id);
                            const shouldTruncate = review.comment.length > 250;
                            const displayComment = isExpanded
                                ? review.comment
                                : review.comment.slice(0, 250) + (shouldTruncate ? "..." : "");

                            const isOwner = review.userId === user?.id;

                            return (
                                <div
                                    key={review.id}
                                    className="border rounded-md p-4 bg-gray-50 flex flex-col min-h-[250px] shadow-lg"
                                >
                                    <div className="flex justify-between items-start space-x-2">
                                        <div>
                                            <p className="text-gray-900">{limitText(review.userName, 23)}</p>
                                            <p className="text-gray-600">{limitText(review.title, 40)}</p>
                                        </div>
                                        <div className="flex items-center gap-2">
                                            <div className="flex items-center justify-center text-l bg-[#32492D] text-white rounded-lg px-4 py-1">
                                                {review.rating}
                                            </div>

                                            {isOwner && (
                                                <DropdownButton
                                                    itemId={review.id}
                                                    menuItems={[
                                                        {
                                                            label: "Delete",
                                                            icon: FaTrash,
                                                            onClick: () => deleteReview.mutate(review.id),
                                                        },
                                                    ]}
                                                />
                                            )}
                                        </div>
                                    </div>

                                    <p className="text-sm text-gray-600 mt-2">
                                        {displayComment}
                                    </p>

                                    {shouldTruncate && (
                                        <div className="mt-1">
                                            <button
                                                onClick={() => toggleExpand(review.id)}
                                                className="text-sm text-[#32492D] font-semibold hover:text-[#273823] transition-all duration-200 ease-in-out transform cursor-pointer"
                                            >
                                                {isExpanded ? "Show less" : "Read more"}
                                            </button>
                                        </div>
                                    )}

                                    {review.response && (
                                        <div className="mt-4 pt-4 border-t bg-white rounded-md">
                                            <div className="flex justify-between items-center">
                                                <span className="text-gray-900">Manager</span>
                                                {isManager && user?.id === review.response.managerId && (
                                                    <DropdownButton
                                                        itemId={review.id}
                                                        menuItems={[
                                                            {
                                                                label: "Delete Response",
                                                                icon: FaTrash,
                                                                onClick: () => deleteResponse.mutate(review.id),
                                                            },
                                                        ]}
                                                    />
                                                )}
                                            </div>

                                            <p className="mt-2 text-gray-600 text-sm">
                                                {expandedReviewIds.includes(`response-${review.id}`)
                                                    ? review.response.comment
                                                    : review.response.comment.slice(0, 70) + (review.response.comment.length > 70 ? "..." : "")}
                                            </p>

                                            {review.response.comment.length > 70 && (
                                                <div className="mt-1">
                                                    <button
                                                        onClick={() =>
                                                            toggleExpand(`response-${review.id}`)
                                                        }
                                                        className="text-sm text-[#32492D] font-semibold hover:text-[#273823] transition-all duration-200 ease-in-out transform cursor-pointer"
                                                    >
                                                        {expandedReviewIds.includes(`response-${review.id}`) ? "Show less" : "Read more"}
                                                    </button>
                                                </div>
                                            )}
                                        </div>
                                    )}

                                    {isManager && !review.response && respondingToId !== review.id && (
                                        <div className="flex justify-center mt-auto pt-2">
                                            <button
                                                onClick={() => setRespondingToId(review.id)}
                                                className="px-4 py-2 min-w-24 text-white bg-[#32492D] rounded-lg hover:bg-[#273823] transition-all duration-300"
                                            >
                                                Respond
                                            </button>
                                        </div>
                                    )}

                                    {isManager && respondingToId === review.id && (
                                        <form
                                            onSubmit={handleSubmitResponse((data) =>
                                                respondToReview.mutate({ reviewId: review.id, comment: data.comment })
                                            )}
                                            className="mt-3 space-y-2"
                                        >
                                            <textarea
                                                {...registerResponse("comment", { required: "Response is required" })}
                                                className="w-full border px-3 py-2 rounded-md
                                                    focus:outline-none transition-all duration-100 ease-in-out transform
                                                    rounded-lg focus:outline-none focus:border-[#32492D] focus:ring-1 focus:ring-[#32492D]"
                                                rows={3}
                                                placeholder="Write a response..."
                                            />
                                            {responseErrors.comment && (
                                                <p className="text-red-500 text-sm">{responseErrors.comment.message}</p>
                                            )}

                                            <div className="flex justify-end gap-2">
                                                <button
                                                    type="button"
                                                    onClick={() => {
                                                        setRespondingToId(null);
                                                        resetResponse();
                                                    }}
                                                    className="px-4 py-2 min-w-24 border rounded-lg text-gray-700 hover:bg-gray-200 transition-all duration-300"
                                                >
                                                    Cancel
                                                </button>
                                                <button
                                                    type="submit"
                                                    className="px-4 py-2 min-w-24 text-white bg-[#32492D] rounded-lg hover:bg-[#273823] transition-all duration-300"
                                                >
                                                    Submit
                                                </button>
                                            </div>
                                        </form>
                                    )}
                                </div>
                            );
                        })}
                    </div>
                ) : (
                    <div className="text-center text-gray-600">No reviews yet.</div>
                )}
            </div>

            {isUser && !isAdding && !userAlreadyReviewed && (
                <div className="flex items-center justify-center pt-4">
                    <button
                        className="mt-2 px-6 py-2 bg-[#32492D] text-white rounded-lg hover:bg-[#273823] transition-all w-60"
                        onClick={() => setIsAdding(true)}
                    >
                        Add Review
                    </button>
                </div>
            )}
        </div>
    );
};

export default ReviewSection;
