package com.jpmc.midascore.service;

import com.jpmc.midascore.entity.TransactionRecord;
import com.jpmc.midascore.entity.UserRecord;
import com.jpmc.midascore.foundation.Transaction;
import com.jpmc.midascore.repository.TransactionRepository;
import com.jpmc.midascore.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

@Service
public class TransactionService {

    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;

    public TransactionService(UserRepository userRepository,
                              TransactionRepository transactionRepository) {
        this.userRepository = userRepository;
        this.transactionRepository = transactionRepository;
    }

    @Transactional
    public void process(Transaction transaction) {

        // 1) Load users
        Optional<UserRecord> senderOpt = userRepository.findById(transaction.getSenderId());
        Optional<UserRecord> recipientOpt = userRepository.findById(transaction.getRecipientId());

        if (senderOpt.isEmpty() || recipientOpt.isEmpty()) {
            return; // invalid sender/recipient
        }

        UserRecord sender = senderOpt.get();
        UserRecord recipient = recipientOpt.get();

        BigDecimal amount = toMoney(transaction.getAmount());

        // 2) Validate balance
        if (!hasSufficientFunds(sender, amount)) {
            return; // insufficient funds
        }

        // 3) Update balances
        applyBalanceUpdate(sender, recipient, amount);

        // 4) Record transaction row
        TransactionRecord record = new TransactionRecord(sender, recipient, amount.floatValue());
        transactionRepository.save(record);
    }

    private boolean hasSufficientFunds(UserRecord sender, BigDecimal amount) {
        BigDecimal senderBalance = toMoney(sender.getBalance());
        return senderBalance.compareTo(amount) >= 0;
    }

    private void applyBalanceUpdate(UserRecord sender, UserRecord recipient, BigDecimal amount) {
        BigDecimal senderBalance = toMoney(sender.getBalance());
        BigDecimal recipientBalance = toMoney(recipient.getBalance());

        BigDecimal newSenderBalance = senderBalance.subtract(amount);
        BigDecimal newRecipientBalance = recipientBalance.add(amount);

        sender.setBalance(newSenderBalance.floatValue());
        recipient.setBalance(newRecipientBalance.floatValue());
    }

    private BigDecimal toMoney(float value) {
        // BigDecimal.valueOf avoids some common floating conversion surprises vs new BigDecimal(float)
        return BigDecimal.valueOf(value);
    }
}