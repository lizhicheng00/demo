package com.huawei.devbridge.relaycontroller;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.huawei.devbridge.relaycontroller.application.service.GridConfigAppService;
import com.huawei.devbridge.relaycontroller.application.service.MeteringAppService;
import com.huawei.devbridge.relaycontroller.application.service.NodeAppService;
import com.huawei.devbridge.relaycontroller.application.service.RelayStatusAppService;
import com.huawei.devbridge.relaycontroller.application.service.TokenAppService;
import com.huawei.devbridge.relaycontroller.application.service.TunnelAppService;
import com.huawei.devbridge.relaycontroller.application.service.TunnelPortAppService;
import com.huawei.devbridge.relaycontroller.common.exception.GlobalExceptionHandler;
import com.huawei.devbridge.relaycontroller.interfaces.controller.GridConfigController;
import com.huawei.devbridge.relaycontroller.interfaces.controller.MeteringController;
import com.huawei.devbridge.relaycontroller.interfaces.controller.NodeController;
import com.huawei.devbridge.relaycontroller.interfaces.controller.RelayStatusController;
import com.huawei.devbridge.relaycontroller.interfaces.controller.TokenController;
import com.huawei.devbridge.relaycontroller.interfaces.controller.TunnelController;
import com.huawei.devbridge.relaycontroller.interfaces.controller.TunnelPortController;
import com.huawei.devbridge.relaycontroller.interfaces.request.CreateOttTokenRequest;
import com.huawei.devbridge.relaycontroller.interfaces.request.CreateRtTokenRequest;
import com.huawei.devbridge.relaycontroller.interfaces.request.CreateTunnelPortRequest;
import com.huawei.devbridge.relaycontroller.interfaces.request.CreateTunnelRequest;
import com.huawei.devbridge.relaycontroller.interfaces.request.MeteringReportRequest;
import com.huawei.devbridge.relaycontroller.interfaces.request.RegisterNodeRequest;
import com.huawei.devbridge.relaycontroller.interfaces.request.UpdateTunnelPortRequest;
import com.huawei.devbridge.relaycontroller.interfaces.request.UpdateTunnelRequest;
import com.huawei.devbridge.relaycontroller.interfaces.response.CreateOttTokenResponse;
import com.huawei.devbridge.relaycontroller.interfaces.response.CreateRtTokenResponse;
import com.huawei.devbridge.relaycontroller.interfaces.response.CreateTunnelResponse;
import com.huawei.devbridge.relaycontroller.interfaces.response.GatewayTunnelPortPolicyResponse;
import com.huawei.devbridge.relaycontroller.interfaces.response.GridConfigResponse;
import com.huawei.devbridge.relaycontroller.interfaces.response.MeteringReportResponse;
import com.huawei.devbridge.relaycontroller.interfaces.response.NodeInfoResponse;
import com.huawei.devbridge.relaycontroller.interfaces.response.RegisterNodeResponse;
import com.huawei.devbridge.relaycontroller.interfaces.response.RelayStatusResponse;
import com.huawei.devbridge.relaycontroller.interfaces.response.TunnelDetailResponse;
import com.huawei.devbridge.relaycontroller.interfaces.response.TunnelListItemResponse;
import com.huawei.devbridge.relaycontroller.interfaces.response.TunnelPortResponse;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(MockitoExtension.class)
class RelayControllerApiTest {
    private static final String BASE = "/open-api-inner/v1/relay-controller";
    private static final String USER_ID = "user-001";
    private static final String TUNNEL_ID = "000001e240";
    private static final String GRID_NAME = "grid-a";

    private MockMvc mockMvc;

    @Mock
    private TunnelAppService tunnelAppService;
    @Mock
    private NodeAppService nodeAppService;
    @Mock
    private GridConfigAppService gridConfigAppService;
    @Mock
    private MeteringAppService meteringAppService;
    @Mock
    private RelayStatusAppService relayStatusAppService;
    @Mock
    private TunnelPortAppService tunnelPortAppService;
    @Mock
    private TokenAppService tokenAppService;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(
                        new TunnelController(tunnelAppService),
                        new NodeController(nodeAppService),
                        new GridConfigController(gridConfigAppService),
                        new MeteringController(meteringAppService),
                        new RelayStatusController(relayStatusAppService),
                        new TunnelPortController(tunnelPortAppService),
                        new TokenController(tokenAppService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void createTunnelApi() throws Exception {
        when(tunnelAppService.createTunnel(eq(USER_ID), any(CreateTunnelRequest.class)))
                .thenReturn(createTunnelResponse());

        mockMvc.perform(post(BASE + "/tunnel")
                        .header("X-User-Id", USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "dev",
                                  "gridname": "grid-a",
                                  "cluster": "cluster-a",
                                  "type": "bridge"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.tunnelId").value(TUNNEL_ID))
                .andExpect(jsonPath("$.data.gridname").value(GRID_NAME));
    }

    @Test
    void createTunnelWithoutUserHeaderReturnsUnauthorized() throws Exception {
        mockMvc.perform(post(BASE + "/tunnel")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "dev",
                                  "gridname": "grid-a",
                                  "type": "bridge"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(40100))
                .andExpect(jsonPath("$.message").value("X-User-Id is required"));
    }

    @Test
    void listTunnelsApi() throws Exception {
        when(tunnelAppService.listTunnels(USER_ID, GRID_NAME)).thenReturn(List.of(
                TunnelListItemResponse.builder()
                        .name("dev")
                        .description("dev tunnel")
                        .created(1720000000L)
                        .url("000001e240.region-a.relayprovider.xxx.com")
                        .build()));

        mockMvc.perform(get(BASE + "/tunnels")
                        .header("X-User-Id", USER_ID)
                        .param("gridName", GRID_NAME))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].name").value("dev"));
    }

    @Test
    void getTunnelDetailApi() throws Exception {
        when(tunnelAppService.getTunnelDetail(USER_ID, TUNNEL_ID)).thenReturn(TunnelDetailResponse.builder()
                .name("dev")
                .id(TUNNEL_ID)
                .tunnelId(TUNNEL_ID)
                .tunnelCode(123456L)
                .gridName(GRID_NAME)
                .url("000001e240.region-a.relayprovider.xxx.com")
                .type("bridge")
                .build());

        mockMvc.perform(get(BASE + "/tunnel")
                        .header("X-User-Id", USER_ID)
                        .param("tunnelId", TUNNEL_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.id").value(TUNNEL_ID))
                .andExpect(jsonPath("$.data.gridname").value(GRID_NAME));
    }

    @Test
    void updateTunnelApi() throws Exception {
        when(tunnelAppService.updateTunnel(eq(USER_ID), any(UpdateTunnelRequest.class))).thenReturn(true);

        mockMvc.perform(put(BASE + "/tunnel")
                        .header("X-User-Id", USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "tunnelId": "000001e240",
                                  "name": "dev-renamed"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data").value(true));
    }

    @Test
    void deleteTunnelApi() throws Exception {
        when(tunnelAppService.deleteTunnel(USER_ID, TUNNEL_ID)).thenReturn(true);

        mockMvc.perform(delete(BASE + "/tunnel")
                        .header("X-User-Id", USER_ID)
                        .param("tunnelId", TUNNEL_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data").value(true));
    }

    @Test
    void registerNodeApi() throws Exception {
        when(nodeAppService.registerNode(eq(GRID_NAME), any(RegisterNodeRequest.class))).thenReturn(
                RegisterNodeResponse.builder()
                        .nodeId("000f")
                        .nodeList(List.of("10.0.1.23"))
                        .build());

        mockMvc.perform(post(BASE + "/grids/{gridName}/nodes/register", GRID_NAME)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "ip": "10.0.1.23"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.nodeId").value("000f"))
                .andExpect(jsonPath("$.data.nodeList", contains("10.0.1.23")));
    }

    @Test
    void getNodeApi() throws Exception {
        when(nodeAppService.getNode(GRID_NAME, "000f"))
                .thenReturn(NodeInfoResponse.builder().ip("10.0.1.23").build());

        mockMvc.perform(get(BASE + "/grids/{gridName}/nodes", GRID_NAME)
                        .param("node_id", "000f"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.ip").value("10.0.1.23"));
    }

    @Test
    void getGridConfigApi() throws Exception {
        when(gridConfigAppService.getConfig(GRID_NAME)).thenReturn(GridConfigResponse.builder()
                .jwtPublicKeys(Map.of("1", "public-key"))
                .build());

        mockMvc.perform(get(BASE + "/grids/{gridName}/config", GRID_NAME))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.jwtPublicKeys.1").value("public-key"));
    }

    @Test
    void reportMeteringApi() throws Exception {
        when(meteringAppService.report(eq(GRID_NAME), any(MeteringReportRequest.class)))
                .thenReturn(MeteringReportResponse.builder().accepted(true).build());

        mockMvc.perform(post(BASE + "/grids/{gridName}/metering", GRID_NAME)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "tunnelCode": 123456,
                                  "tunnelId": "000001e240",
                                  "usage": 1024
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.accepted").value(true));
    }

    @Test
    void getRelayStatusApi() throws Exception {
        when(relayStatusAppService.getStatus(USER_ID, TUNNEL_ID)).thenReturn(RelayStatusResponse.builder()
                .tunnelId(TUNNEL_ID)
                .status("ONLINE")
                .gridName(GRID_NAME)
                .nodeId("000f")
                .gatewayIp("10.0.1.23")
                .lastHeartbeat(1720000000L)
                .build());

        mockMvc.perform(get(BASE + "/tunnel/status")
                        .header("X-User-Id", USER_ID)
                        .param("tunnelId", TUNNEL_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.status").value("ONLINE"))
                .andExpect(jsonPath("$.data.gridname").value(GRID_NAME));
    }

    @Test
    void createTunnelPortApi() throws Exception {
        when(tunnelPortAppService.create(eq(USER_ID), eq(TUNNEL_ID), any(CreateTunnelPortRequest.class)))
                .thenReturn(tunnelPortResponse(false));

        mockMvc.perform(post(BASE + "/tunnels/{tunnelId}/ports", TUNNEL_ID)
                        .header("X-User-Id", USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "port": 8080,
                                  "allowAnonymous": false
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.port").value(8080))
                .andExpect(jsonPath("$.data.allowAnonymous").value(false));
    }

    @Test
    void listTunnelPortsApi() throws Exception {
        when(tunnelPortAppService.list(USER_ID, TUNNEL_ID)).thenReturn(List.of(
                tunnelPortResponse(false),
                TunnelPortResponse.builder()
                        .id(2L)
                        .tunnelId(TUNNEL_ID)
                        .tunnelCode(123456L)
                        .port(8888L)
                        .allowAnonymous(true)
                        .build()));

        mockMvc.perform(get(BASE + "/tunnels/{tunnelId}/ports", TUNNEL_ID)
                        .header("X-User-Id", USER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data", hasSize(2)))
                .andExpect(jsonPath("$.data[1].allowAnonymous").value(true));
    }

    @Test
    void getTunnelPortApi() throws Exception {
        when(tunnelPortAppService.detail(USER_ID, TUNNEL_ID, 8080L)).thenReturn(tunnelPortResponse(false));

        mockMvc.perform(get(BASE + "/tunnels/{tunnelId}/ports/{port}", TUNNEL_ID, 8080)
                        .header("X-User-Id", USER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.tunnelId").value(TUNNEL_ID))
                .andExpect(jsonPath("$.data.port").value(8080));
    }

    @Test
    void updateTunnelPortApi() throws Exception {
        when(tunnelPortAppService.update(eq(USER_ID), eq(TUNNEL_ID), eq(8080L), any(UpdateTunnelPortRequest.class)))
                .thenReturn(tunnelPortResponse(true));

        mockMvc.perform(put(BASE + "/tunnels/{tunnelId}/ports/{port}", TUNNEL_ID, 8080)
                        .header("X-User-Id", USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "allowAnonymous": true
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.allowAnonymous").value(true));
    }

    @Test
    void deleteTunnelPortApi() throws Exception {
        when(tunnelPortAppService.delete(USER_ID, TUNNEL_ID, 8080L)).thenReturn(true);

        mockMvc.perform(delete(BASE + "/tunnels/{tunnelId}/ports/{port}", TUNNEL_ID, 8080)
                        .header("X-User-Id", USER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data").value(true));
    }

    @Test
    void getGatewayTunnelPortPolicyApi() throws Exception {
        when(tunnelPortAppService.getGatewayPortPolicy(GRID_NAME, TUNNEL_ID, 8080L))
                .thenReturn(GatewayTunnelPortPolicyResponse.builder()
                        .tunnelId(TUNNEL_ID)
                        .tunnelCode(123456L)
                        .gridName(GRID_NAME)
                        .port(8080L)
                        .allowAnonymous(false)
                        .build());

        mockMvc.perform(get(BASE + "/grids/{gridName}/tunnels/{tunnelId}/ports/{port}", GRID_NAME, TUNNEL_ID, 8080))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.gridname").value(GRID_NAME))
                .andExpect(jsonPath("$.data.allowAnonymous").value(false));
    }

    @Test
    void createOttTokenApi() throws Exception {
        when(tokenAppService.createOtt(any(CreateOttTokenRequest.class))).thenReturn(CreateOttTokenResponse.builder()
                .tokenType("OTT")
                .token("ott-token")
                .expiresIn(1800L)
                .build());

        mockMvc.perform(post(BASE + "/tokens/ott")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "tunnelId": "000001e240",
                                  "gridname": "grid-a",
                                  "connId": "conn-1"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.tokenType").value("OTT"))
                .andExpect(jsonPath("$.data.expiresIn").value(1800));
    }

    @Test
    void createRtTokenApi() throws Exception {
        when(tokenAppService.createRt(eq("Bearer ott-token"), eq(USER_ID), any(CreateRtTokenRequest.class)))
                .thenReturn(CreateRtTokenResponse.builder()
                        .tokenType("RT")
                        .token("rt-token")
                        .expiresIn(86400L)
                        .build());

        mockMvc.perform(post(BASE + "/tokens/rt")
                        .header("X-Relay-Authorization", "Bearer ott-token")
                        .header("X-User-Id", USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "tunnelId": "000001e240"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.tokenType").value("RT"))
                .andExpect(jsonPath("$.data.expiresIn").value(86400));
    }

    private CreateTunnelResponse createTunnelResponse() {
        return CreateTunnelResponse.builder()
                .name("dev")
                .id(TUNNEL_ID)
                .tunnelId(TUNNEL_ID)
                .tunnelCode(123456L)
                .gridName(GRID_NAME)
                .cluster("cluster-a")
                .bandwidthUsed(0L)
                .expiration(1720086400)
                .created(1720000000L)
                .url("000001e240.region-a.relayprovider.xxx.com")
                .type("bridge")
                .build();
    }

    private TunnelPortResponse tunnelPortResponse(boolean allowAnonymous) {
        return TunnelPortResponse.builder()
                .id(1L)
                .tunnelId(TUNNEL_ID)
                .tunnelCode(123456L)
                .port(8080L)
                .allowAnonymous(allowAnonymous)
                .build();
    }
}
