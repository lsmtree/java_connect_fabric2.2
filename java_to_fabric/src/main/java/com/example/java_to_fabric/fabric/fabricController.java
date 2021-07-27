package com.example.java_to_fabric.fabric;

import org.hyperledger.fabric.sdk.*;
import org.hyperledger.fabric.sdk.exception.InvalidArgumentException;
import org.hyperledger.fabric.sdk.exception.ProposalException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;

@Controller
public class fabricController {
    @Resource(name = "getHFClient")
    private HFClient client;
    @Resource(name = "getChannel")
    private Channel channel;

    @RequestMapping("getAllCars")
    @ResponseBody
    public String getAll() throws InvalidArgumentException, ProposalException {
        QueryByChaincodeRequest qcr = client.newQueryProposalRequest();
        //setFcn:要执行的链码查询方法
        //setChaincodeName:设置成你自己的链码名
        qcr.setFcn("queryAllCars").setChaincodeName("fabcar");

        Collection<ProposalResponse> responses = null;
        try {
            responses = channel.queryByChaincode(qcr);
        } catch (InvalidArgumentException e) {
            throw new InvalidArgumentException("合约名或方法名错误", e);
        } catch (ProposalException e) {
            throw new ProposalException("请求发送失败", e);
        }
        return  responses.iterator().next().getProposalResponse().getResponse().getPayload().toStringUtf8();
    }

    @GetMapping("/getCarByKey")
    @ResponseBody
    public String getCarByname(@RequestParam String key) throws InvalidArgumentException, ProposalException {
        QueryByChaincodeRequest qcr = client.newQueryProposalRequest();
        qcr.setFcn("queryCar").setArgs(key).setChaincodeName("fabcar");
        Collection<ProposalResponse> responses = null;

        try {
            responses = channel.queryByChaincode(qcr);
        } catch (InvalidArgumentException e) {
            throw new InvalidArgumentException("合约名或方法名错误",e);
        } catch (ProposalException e) {
            throw new ProposalException("请求放松失败",e);
        }
        return responses.iterator().next().getProposalResponse().getResponse().getPayload().toStringUtf8();
    }

    @RequestMapping("/createCar")
    @ResponseBody
    public String creCar() throws ProposalException, InvalidArgumentException {
        TransactionProposalRequest tpr = client.newTransactionProposalRequest();
        tpr.setFcn("createCar").setArgs("CAR20","localMake","localModel","localColor","lx").setChaincodeName("fabcar");
        Collection<ProposalResponse> responses = null;
        try {
            responses  =channel.sendTransactionProposal(tpr);
        } catch (ProposalException e) {
            throw new ProposalException("请求发送失败",e);
        } catch (InvalidArgumentException e) {
            throw new InvalidArgumentException("合约名或者方法名错误");
        }
        String result = responses.iterator().next().getProposalResponse().getResponse().getPayload().toStringUtf8();
        CompletableFuture<BlockEvent.TransactionEvent> future = channel.sendTransaction(responses);
        return result;
    }

}
