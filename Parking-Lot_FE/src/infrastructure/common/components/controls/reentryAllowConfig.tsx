import React from 'react';
import Constants from '../../../../core/common/constants';
type Props = {
    reentryAllowed: boolean
}
export const AllowConfig = ({ reentryAllowed }: Props) => {
    switch (reentryAllowed) {
        case Constants.ReentryAllowed.Allowed.value:
            return (
                <div
                    className='bg-[#74f1a6] 
                    text-[#3b3b3b] px-2 py-1
                    font-semibold text-center
                    rounded-[2px]'
                >
                    {Constants.ReentryAllowed.Allowed.label}
                </div>
            )
        case Constants.ReentryAllowed.NotAllowed.value:
            return (
                <div
                    className='bg-[#ff7b7b] 
                    text-[#FFFFFF] px-2 py-1
                    font-semibold text-center
                    rounded-[2px]'
                >
                    {Constants.ReentryAllowed.NotAllowed.label}
                </div>
            )
        default:
            return <div></div>
    }
}
