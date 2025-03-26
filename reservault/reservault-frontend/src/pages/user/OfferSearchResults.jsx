import { useSearchParams } from "react-router-dom";
import { useQuery } from "@tanstack/react-query";
import { useState } from "react";
import api from "../../api/axios";
import SmartOfferSearch from "../../components/user/SmartOfferSearch";
import OfferList from "../../components/manager/OfferList";
import Header from "../../components/common/Header";

const OfferSearchResults = () => {
    const [searchParams] = useSearchParams();

    const [selectedOfferDetails, setSelectedOfferDetails] = useState(null);

    const {
        data: offers,
        isLoading,
        error,
    } = useQuery({
        queryKey: ["offers", "search", searchParams.toString()],
        queryFn: async () => {
            const { data } = await api.get(`/offers/search?${searchParams.toString()}`);
            return data;
        },
    });

    return (
        <>
            <Header />

            <div className="container mx-auto py-8 max-w-6xl">
                <SmartOfferSearch />
            </div>

            <div className="container mx-auto pb-12 max-w-6xl">
                <OfferList
                    offers={offers}
                    isLoading={isLoading}
                    error={error}
                    onModify={null}
                />
            </div>
        </>
    );
};

export default OfferSearchResults;