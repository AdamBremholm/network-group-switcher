import ApiService from "./api.service";

class ConnectionError extends Error {
  constructor(errorCode, message) {
    super(message);
    this.name = this.constructor.name;
    this.message = message;
    this.errorCode = errorCode;
  }
}

const AliasService = {
  async loadAliases() {
    try {
      return await ApiService.get("/api/network/aliases");
    } catch (error) {
      throw new ConnectionError(
        error.response.status,
        error.response.data.detail
      );
    }
  },


  async sendHost({state}, action) {
    try {
      const res = await this.putHost(state.aliasList[action.target].hosts[action.newIndex].id, state.aliasList[action.target].hosts[action.newIndex]);
      return res;
    } catch (e) {
      if (e instanceof ConnectionError) {
        console.log("problem with putting at backend, ==> sending error upwards");
        throw new ConnectionError(error.response.status, error.response.detail);
      }
    }
  },

  async putHost(IDParam, host) {
    try {
      return await ApiService.put("/api/network/hosts/" + IDParam, host)
    }catch (error) {
      throw new ConnectionError(error.response.status, error.response.detail);
    }
  },

  async updateAliasTables() {
    try {
       const res = await ApiService.get("/api/v1/aliases/update_urltables");
       console.log(res.data.data.updates);
       return res;
    } catch (error) {
      throw new ConnectionError(error.response.status, error.response.detail);
    }
  }
};

export default AliasService;

export { AliasService };
