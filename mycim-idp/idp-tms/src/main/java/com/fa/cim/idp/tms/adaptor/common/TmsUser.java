package com.fa.cim.idp.tms.adaptor.common;

import com.fa.cim.common.support.User;
import com.fa.cim.idp.tms.adaptor.TmsAdapt;
import lombok.Data;

/**
 * description:
 * <p>TmsUser .<br/></p>
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2019/4/12        ********             Yuri               create file
 *
 * @author: Yuri
 * @date: 2019/4/12 15:50
 * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Data
public class TmsUser implements TmsAdapt<User, TmsUser>{

	private static final long serialVersionUID = 604947394920532785L;
	/**
	 * User ID
	 */
	private TmsIdentifier userID;
	/**
	 * Password
	 */
	private String password;
	/**
	 * New Password
	 */
	private String newPassword;
	/**
	 * Function ID. For example, the Function ID of TxFutureHoldReq is "TXPC041".
	 */
	private String functionID;
	/**
	 * Client Node
	 */
	private String clientNode;
	/**
	 * Reserved for SI customization
	 */
	private Object reserve;

	@Override
	public User adapt() {
		User user = new User();
		user.setClientNode(this.clientNode);
		user.setFunctionID(this.functionID);
		user.setPassword(this.password);
		user.setNewPassword(this.newPassword);
		user.setReserve(this.reserve);
		user.setUserID(this.userID.adapt());
		return user;
	}

	@Override
	public TmsUser from(User obj) {
		this.userID = new TmsIdentifier().from(obj.getUserID());
		this.password = obj.getPassword();
		this.newPassword = obj.getNewPassword();
		this.functionID = obj.getFunctionID();
		this.clientNode = obj.getClientNode();
		this.reserve = obj.getReserve();
		return this;
	}
}
