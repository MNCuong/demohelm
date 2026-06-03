package com.example.web_monitor.service;

import com.example.web_monitor.model.entities.TxMsgLogDetail;
import com.example.web_monitor.repository.TxMsgLogDetailRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TxMsgLogDetailService {
    private final TxMsgLogDetailRepository txMsgLogDetailRepository;

    public TxMsgLogDetailService(TxMsgLogDetailRepository txMsgLogDetailRepository) {
        this.txMsgLogDetailRepository = txMsgLogDetailRepository;
    }

    public TxMsgLogDetail getMaxId() {
        return txMsgLogDetailRepository.findTopByOrderByAutoIdDesc();
    }

//    public List<TxMsgLogDetail> findByAutoIdGreaterThanAndStatusIn(Long lastId, List<String> statuses) {
//        return txMsgLogDetailRepository.findByAutoIdGreaterThanAndStatusIn(
//                lastId, statuses);
//    }

    public List<TxMsgLogDetail>
    findInMsgDetailForLatency(
            Long lastAutoId,
            List<String> statuses) {

        return txMsgLogDetailRepository.findInMsgDetailForLatency(
                lastAutoId,
                statuses
        );
    }
}
