package io.keyko.nevermined.manager;

import io.keyko.common.helpers.EncodingHelper;
import io.keyko.common.web3.KeeperService;
import io.keyko.nevermined.exceptions.DDOException;
import io.keyko.nevermined.exceptions.EthereumException;
import io.keyko.nevermined.exceptions.NftException;
import io.keyko.nevermined.exceptions.ServiceException;
import io.keyko.nevermined.external.MetadataApiService;
import io.keyko.nevermined.models.DDO;
import io.keyko.nevermined.models.DID;
import io.keyko.nevermined.models.asset.AssetMetadata;
import io.keyko.nevermined.models.metadata.SearchQuery;
import io.keyko.nevermined.models.metadata.SearchResult;
import org.web3j.abi.EventEncoder;
import org.web3j.abi.datatypes.Event;
import org.web3j.crypto.Keys;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.request.EthFilter;
import org.web3j.protocol.core.methods.response.EthLog;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tuples.generated.Tuple6;
import org.web3j.utils.Numeric;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Manages the functionality related with Assets
 */
public class AssetsManager extends BaseManager {

    public AssetsManager(KeeperService keeperService, MetadataApiService metadataApiService) {
        super(keeperService, metadataApiService);
    }

    /**
     * Gets an instance of AssetManager
     *
     * @param keeperService   instance of keeperService
     * @param metadataApiService instance of MetadataApiService
     * @return an initialized instance of AssetManager
     */
    public static AssetsManager getInstance(KeeperService keeperService, MetadataApiService metadataApiService) {
        return new AssetsManager(keeperService, metadataApiService);
    }

    /**
     * Publishes in Metadata Api the metadata of a DDO
     *
     * @param ddo the DDO to publish
     * @return the published DDO
     * @throws Exception if Metadata Api service fails publishing the DDO
     */
    public DDO publishMetadata(DDO ddo) throws Exception {
        return getMetadataApiService().createDDO(ddo);
    }


    /**
     * Gets a DDO from the DID
     *
     * @param id the did of the DDO
     * @return an instance of the DDO represented by the DID
     * @throws Exception if Metadata Api service fails publishing the metadata
     */
    public DDO getByDID(String id) throws Exception {
        return getMetadataApiService().getDDOUsingId(id);
    }

    /**
     * Updates the metadata of a DDO
     *
     * @param id  the did of the DDO
     * @param ddo the DDO
     * @return A flag that indicates if the update was executed correctly
     * @throws Exception if Metadata Api service fails updating the metadata
     */
    public boolean updateMetadata(String id, DDO ddo) throws Exception {
        return getMetadataApiService().updateDDO(id, ddo);
    }

    /**
     * Gets all the DDOs that match the search criteria
     *
     * @param text   contains the criteria
     * @param offset parameter to paginate the results
     * @param page   parameter to paginate the results
     * @return SearchResult including the list of DDOs
     * @throws DDOException if Aquairus fails searching the assets
     */
    public SearchResult searchAssets(String text, int offset, int page) throws DDOException {
        return getMetadataApiService().searchDDO(text, offset, page);
    }

    /**
     * Gets all the DDOs that match the parameters of the query
     *
     * @param params contains the criteria
     * @param offset parameter to paginate the results
     * @param page   parameter to paginate the results
     * @param sort   parameter to sort the results
     * @return a List with all the DDOs found
     * @throws DDOException if Aquairus fails searching the assets
     */
    public SearchResult searchAssets(Map<String, Object> params, int offset, int page, int sort) throws DDOException {
        SearchQuery searchQuery = new SearchQuery(params, offset, page, sort);
        return getMetadataApiService().searchDDO(searchQuery);
    }

    /**
     * Retire the asset ddo from Metadata Api.
     *
     * @param did the did
     * @return a flag that indicates if the retire operation was executed correctly
     * @throws DDOException DDOException
     */
    public Boolean deleteAsset(DID did) throws DDOException {
        return getMetadataApiService().retireAssetDDO(did.getDid());

    }

    /**
     * Check that the metadata has a valid formUrl.
     *
     * @param metadata the metadata of the DDO
     * @return a flag that indicates if the metadata is valid
     * @throws DDOException DDOException
     */
    public Boolean validateMetadata(AssetMetadata metadata) throws DDOException {
        return getMetadataApiService().validateMetadata(metadata);

    }

    /**
     * Get the owner of a did already registered.
     *
     * @param did the did
     * @return owner address
     * @throws Exception Exception
     */
    public String getDIDOwner(DID did) throws Exception {
        return Keys.toChecksumAddress(this.didRegistry.getDIDOwner(EncodingHelper.hexStringToBytes(did.getHash())).send());
    }


    /**
     * List of Asset objects published by ownerAddress
     *
     * @param ownerAddress ethereum address of owner/publisher
     * @return list of dids
     * @throws ServiceException ServiceException
     */
    public List<DID> getOwnerAssets(String ownerAddress) throws ServiceException {
        EthFilter didFilter = new EthFilter(
                DefaultBlockParameterName.EARLIEST,
                DefaultBlockParameterName.LATEST,
                didRegistry.getContractAddress()
        );
        try {

            final Event event = didRegistry.DIDATTRIBUTEREGISTERED_EVENT;
            final String eventSignature = EventEncoder.encode(event);
            didFilter.addSingleTopic(eventSignature);
            didFilter.addNullTopic();
            didFilter.addOptionalTopics(Numeric.toHexStringWithPrefixZeroPadded(Numeric.toBigInt(ownerAddress), 64));

            EthLog ethLog;

            try {
                ethLog = getKeeperService().getWeb3().ethGetLogs(didFilter).send();
            } catch (IOException e) {
                throw new EthereumException("Error creating ownerAssets filter.");
            }

            List<EthLog.LogResult> logs = ethLog.getLogs();
            List<DID> DIDlist = new ArrayList<>();
            for (int i = 0; i <= logs.size() - 1; i++) {
                DIDlist.add(DID.getFromHash(Numeric.cleanHexPrefix((((EthLog.LogObject) logs.get(i)).getTopics().get(1)))));
            }
            return DIDlist;

        } catch (Exception ex) {
            log.error("Unable to retrieve assets owned by " + ownerAddress + ex.getMessage());
            throw new ServiceException("Unable to retrieve assets owned by " + ownerAddress + ex.getMessage());
        }
    }

    /**
     * Transfer the ownsership of a DID
     * @param did the did
     * @param newOwnerAddress the address of the new owner
     * @return a flag indicates if the operation has been executed correctly
     * @throws DDOException DDOException
     */
    public Boolean transferOwnership(DID did, String newOwnerAddress) throws DDOException {

        Boolean isOwner;

        try {
            isOwner = getDIDOwner(did).equals(getMainAccount().address);
        }catch (Exception e) {
            throw new DDOException("There was an error trying to check the owner of the DID " + did.getDid(), e);
        }

        if (!isOwner)
            throw new DDOException("The main account is not the owner of this DID");

        try {

            TransactionReceipt receipt = didRegistry
                    .transferDIDOwnership(EncodingHelper.hexStringToBytes(did.getHash()), Keys.toChecksumAddress(newOwnerAddress))
                    .send();

            if (!receipt.getStatus().equals("0x1")) {
                String msg = "The Status received is not valid executing DIDRegistry.transferDIDOwnership: " + receipt.getStatus();
                log.error(msg);
                throw new DDOException(msg);
            }

            return true;

        }catch (Exception e){
            throw new DDOException("There was an error trying to transfer the ownership of the DID " + did.getDid(), e);
        }

    }

    public Boolean grantPermission(DID did, String subjectAddress) throws DDOException {

        Boolean isOwner;

        try {
            isOwner = getDIDOwner(did).equals(getMainAccount().address);
        }catch (Exception e) {
            throw new DDOException("There was an error trying to check the owner of the DID " + did.getDid(), e);
        }

        if (!isOwner)
            throw new DDOException("The main account is not the owner of this DID");

        try {

            TransactionReceipt receipt = didRegistry
                    .grantPermission(EncodingHelper.hexStringToBytes(did.getHash()), Keys.toChecksumAddress(subjectAddress))
                    .send();

            if (!receipt.getStatus().equals("0x1")) {
                String msg = "The Status received is not valid executing DIDRegistry.grantPermission: " + receipt.getStatus();
                log.error(msg);
                throw new DDOException(msg);
            }

            return true;

        }catch (Exception e){
            throw new DDOException("There was an error trying to grantPermission the Permission for DID " + did.getDid(), e);
        }

    }

    /**
     * revoke the permission over an DID for a subject address
     * @param did the did
     * @param subjectAddress the address
     * @return a flag indicates if the operation has been executed correctly
     * @throws DDOException DDOException
     */
    public Boolean revokePermission(DID did, String subjectAddress) throws DDOException {

        Boolean isOwner;

        try {
            isOwner = getDIDOwner(did).equals(getMainAccount().address);
        }catch (Exception e) {
            throw new DDOException("There was an error trying to check the owner of the DID " + did.getDid(), e);
        }

        if (!isOwner)
            throw new DDOException("The main account is not the owner of this DID");

        try {

            TransactionReceipt receipt = didRegistry
                    .revokePermission(EncodingHelper.hexStringToBytes(did.getHash()), Keys.toChecksumAddress(subjectAddress))
                    .send();

            if (!receipt.getStatus().equals("0x1")) {
                String msg = "The Status received is not valid executing DIDRegistry.revokePermission: " + receipt.getStatus();
                log.error(msg);
                throw new DDOException(msg);
            }

            return true;

        }catch (Exception e){
            throw new DDOException("There was an error trying to revoke the Permission for DID " + did.getDid(), e);
        }

    }

    public Boolean getPermission(DID did, String subjectAddress) throws DDOException {

        try {

            return didRegistry
                    .getPermission(EncodingHelper.hexStringToBytes(did.getHash()), Keys.toChecksumAddress(subjectAddress))
                    .send();

        } catch (Exception e){
            throw new DDOException("There was an error trying to transfer the ownership of the DID " + did.getDid(), e);
        }
    }

    public Boolean addProvider(DID did, String providerAddress) throws EthereumException {
        TransactionReceipt receipt;
        try {
            receipt = didRegistry
                    .addDIDProvider(
                            EncodingHelper.hexStringToBytes(did.getHash()),
                            Keys.toChecksumAddress(providerAddress))
                    .send();

            if (!receipt.getStatus().equals("0x1")) {
                String msg = "The Status received is not valid executing DIDRegistry.addDIDProvider: " + receipt.getStatus();
                log.error(msg);
                throw new EthereumException(msg);
            }
        } catch (Exception e) {
            throw new EthereumException("There was an error trying to add a provider to a DID " + did.getDid(), e);
        }
        return true;
    }

    public Boolean removeProvider(DID did, String providerAddress) throws EthereumException {
        TransactionReceipt receipt;
        try {
            receipt = didRegistry
                    .removeDIDProvider(
                            EncodingHelper.hexStringToBytes(did.getHash()),
                            Keys.toChecksumAddress(providerAddress))
                    .send();

            if (!receipt.getStatus().equals("0x1")) {
                String msg = "The Status received is not valid executing DIDRegistry.removeDIDProvider: " + receipt.getStatus();
                log.error(msg);
                throw new EthereumException(msg);
            }
        } catch (Exception e) {
            throw new EthereumException("There was an error trying to remove a provider from a DID " + did.getDid(), e);
        }
        return true;
    }

    public List<String> listProviders(DID did) throws EthereumException {
        try {

            final Tuple6<String, byte[], String, String, BigInteger, List<String>> didRegisterTuple = didRegistry
                    .getDIDRegister(EncodingHelper.hexStringToBytes(did.getHash()))
                    .send();
            List<String> providers = didRegisterTuple.component6();
            providers.remove("0x0000000000000000000000000000000000000000");
            return providers;
        } catch (Exception e){
            throw new EthereumException("Unable to retrieve providers for the DID " + did.getDid(), e);
        }
    }


    /**
     * Given a DID, the owner of the DID can mint an amount of NFT
     * @param did DID with the nft associated
     * @param amount amount to mint
     * @return true if worked
     * @throws NftException Unable to mint
     */
    public boolean mint(DID did, BigInteger amount) throws NftException {
        try {
            didRegistry.mint(
                    EncodingHelper.hexStringToBytes(did.getHash()),
                    amount)
                    .send();
        } catch (Exception e) {
            String msg = "Error minting token ";
            log.error(msg + ": " + e.getMessage());
            throw new NftException(msg, e);
        }
        return true;
    }

    /**
     * Given a DID, the owner of the DID can burn an amount of NFT
     * @param did DID with the nft associated
     * @param amount amount to burn
     * @return true if worked
     * @throws NftException Unable to burn
     */
    public boolean burn(DID did, BigInteger amount) throws NftException {
        try {
            didRegistry.burn(
                    EncodingHelper.hexStringToBytes(did.getHash()),
                    amount)
                    .send();
        } catch (Exception e) {
            String msg = "Error burning token ";
            log.error(msg + ": " + e.getMessage());
            throw new NftException(msg, e);
        }
        return true;
    }

    /**
     * Allows a DID owner to transfer a specific amount of NFT associated with the DID
     *
     * @param did the DID associated to the NFT
     * @param address the receiver
     * @param amount the amount to transfer to the NFT DID
     * @return true if everything worked
     * @throws NftException Unable to transfer
     */
    public boolean transfer(DID did, String address, BigInteger amount) throws NftException {
        try {
            didRegistry.safeTransferFrom(
                    getKeeperService().getAddress(),
                    address,
                    Numeric.toBigInt(did.getHash()),
                    amount,
                    "".getBytes())
            .send();
        } catch (Exception e) {
            String msg = "Error transferring token ";
            log.error(msg + ": " + e.getMessage());
            throw new NftException(msg, e);
        }
        return true;
    }

    /**
     * Gets the balance of the NFT associated to a DID
     *
     * @param address the address holding the NFT
     * @param did the DID associated to the NFT
     * @return BigInteger the address and DID/NFT balance
     * @throws NftException unable to get the balance
     */
    public BigInteger balance(String address, DID did) throws NftException {
        try {
            return didRegistry.balanceOf(
                    address,
                    EncodingHelper.hexStringToBytes(did.getHash()))
                    .send();
        } catch (Exception e) {
            String msg = "Error getting balance ";
            log.error(msg + ": " + e.getMessage());
            throw new NftException(msg, e);
        }
    }

}
