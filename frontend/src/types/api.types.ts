export interface AdminTokenDto {
  /**
   *
   * @type {string}
   * @memberof AdminTokenDto
   */
  adminToken?: string;
}

export interface CreateRoomRequest {
  /**
   *
   * @type {string}
   * @memberof CreateRoomRequest
   */
  username?: string;
  /**
   *
   * @type {string}
   * @memberof CreateRoomRequest
   */
  uuid?: string;
}
export interface RoomDto {
  /**
   *
   * @type {string}
   * @memberof RoomDto
   */
  roomUuid?: string;
  /**
   *
   * @type {string}
   * @memberof RoomDto
   */
  status?: string;
}

export interface UserDto {
  /**
   *
   * @type {string}
   * @memberof UserDto
   */
  name?: string;
  /**
   *
   * @type {RoomDto}
   * @memberof UserDto
   */
  room?: RoomDto;
  /**
   *
   * @type {boolean}
   * @memberof UserDto
   */
  admin?: boolean;
}

export interface WebSocketUrlDto {
  /**
   *
   * @type {string}
   * @memberof WebSocketUrlDto
   */
  wsConnectUrl?: string;
}
