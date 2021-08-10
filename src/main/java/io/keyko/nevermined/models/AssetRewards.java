package io.keyko.nevermined.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.keyko.nevermined.api.helper.AccountsHelper;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

@JsonPropertyOrder(alphabetic = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class AssetRewards extends AbstractModel implements FromJsonToModel {

    @JsonProperty
    public String totalPrice;

    // This map encapsulates the reward to be distributed to each address
    // The map is using the receiver -> amount structure
    @JsonProperty
    public Map<String, String> rewards;

    public String tokenAddress = AccountsHelper.ZERO_ADDRESS;

    public BigInteger numberNFTs = BigInteger.ZERO;

    public AssetRewards()  {
        this.totalPrice = "0";
        this.rewards = new HashMap<>();
        this.tokenAddress = AccountsHelper.ZERO_ADDRESS;
        this.numberNFTs = BigInteger.ZERO;
    }

    public AssetRewards(Map<String, String> _rewards)  {
        this.rewards = _rewards;
        BigInteger amount = BigInteger.ZERO;
        for (String value : _rewards.values()) {
            amount = amount.add(new BigInteger(value));
        }
        this.totalPrice = amount.toString();
    }

    public AssetRewards(String address, String amount)  {
        this.rewards = new HashMap<>();
        if ( new BigInteger(amount).compareTo(BigInteger.ZERO) > 0)
            this.rewards.put(address, amount);
        this.totalPrice = amount;
    }

    public String getReceiversArrayString() {
        String str = "[" +
                String.join("\",\"", rewards.keySet()) +
                "]";
        return str.replace("[", "[\"")
                .replace("]", "\"]");
    }

    public String getAmountsArrayString() {
        String str = "[" +
                String.join("\",\"", rewards.values()) +
                "]";
        return str.replace("[", "[\"")
                .replace("]", "\"]");
    }
}
