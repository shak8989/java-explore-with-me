
package ru.practicum.ewm.request;

import java.util.ArrayList;
import java.util.List;

public class EventRequestStatusUpdateResult {

    private List<ParticipationRequestDto> confirmedRequests =
            new ArrayList<>();

    private List<ParticipationRequestDto> rejectedRequests =
            new ArrayList<>();

    public List<ParticipationRequestDto> getConfirmedRequests() {
        return confirmedRequests;
    }

    public void setConfirmedRequests(
            List<ParticipationRequestDto> confirmedRequests) {
        this.confirmedRequests = confirmedRequests;
    }

    public List<ParticipationRequestDto> getRejectedRequests() {
        return rejectedRequests;
    }

    public void setRejectedRequests(
            List<ParticipationRequestDto> rejectedRequests) {
        this.rejectedRequests = rejectedRequests;
    }
}
