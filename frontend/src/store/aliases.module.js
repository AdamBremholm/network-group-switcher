import AliasService from "../services/aliases.service";
import ApiService from "../services/api.service";
import Auth from "./auth.module";

const state = {
  aliasList: [],
  sending: false,
  sendError: "",
  sendErrorCode: 0,
};

const mutations = {
  setAliases(state, loadedAliases) {
    console.log(loadedAliases)
    state.aliasList = loadedAliases;
  },
  updateList(state, action) {
    let movedHost = state.aliasList[action.origin].hosts.splice(
      action.oldIndex,
      1
    );
    state.aliasList[action.target].hosts.splice(
      action.newIndex,
      0,
      movedHost[0]
    );

    state.aliasList[action.target].hosts[action.newIndex].alias = state.aliasList[action.target].name
  },
  sendRequest(state) {
    state.sending = true;
    state.sendError = "";
    state.sendErrorCode = 0;
  },

  sendSuccess(state, accessToken) {
    state.sending = false;
  },

  sendError(state, { errorCode, errorMessage }) {
    state.sending = false;
    state.sendErrorCode = errorCode;
    state.sendError = errorMessage;
  },
};

// 1: Försöker ladda, fungerar inte 401 interceptorn loggar vi ut.
const actions = {
  async loadAliases({ commit, dispatch }) {
    commit('sendRequest');
    try {
      const result = await AliasService.loadAliases();
      commit("setAliases", result.data);
      commit('sendSuccess');
    } catch (e) {
      commit('sendError', {errorCode: e.errorCode,
        errorMessage: e.message});
      if (e.errorCode === 401) {
        await dispatch("auth/logout", { commit }, { root: true });
      } else {
        console.log("api unavailable at this time");
      }
    }
  },


  async sendUpdatedHost({ state, commit, dispatch }, action) {
    commit('sendRequest');
    try {
      const res = await AliasService.sendHost({ state }, action);
      commit('sendSuccess');
      return res;
    } catch (e) {
      console.log(e.errorCode + ", " + e.message + "trying to reload aliases");
      await dispatch("aliases/loadAliases", { commit, dispatch }, { root: true });
      commit('sendError', {errorCode: e.errorCode,
        errorMessage: e.message});
    }
  },

  async updateAliasTables({commit}) {
    commit('sendRequest');
    try {
      await AliasService.updateAliasTables();
      commit('sendSuccess');
    }catch (e) {
      console.log(e.errorCode + ", " + e.message);
      commit('sendError', {errorCode: e.errorCode,
        errorMessage: e.message});
    }
  }
};


export const aliases = {
  namespaced: true,
  state,
  actions,
  mutations
};
