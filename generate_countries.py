import urllib.request
import json
import random

try:
    req = urllib.request.Request('https://restcountries.com/v3.1/all', headers={'User-Agent': 'Mozilla/5.0'})
    with urllib.request.urlopen(req) as response:
        raw_data = json.loads(response.read().decode())
        
        if isinstance(raw_data, dict):
            data = list(raw_data.values())
        else:
            data = raw_data
            
        servers = []
        servers.append('        VpnServer("local", "Smart DNS Only (AdBlock)", "IL", "127.0.0.1", 1)')
        for country in sorted([c for c in data if isinstance(c, dict)], key=lambda x: x.get('name', {}).get('common', '')):
            code = country.get('cca2', 'XX')
            name = country.get('name', {}).get('common', 'Unknown').replace('"', '\\"')
            if name == 'Unknown': continue
            id = code.lower()
            ip = f'104.28.{random.randint(10,250)}.{random.randint(1,250)}'
            latency = random.randint(30, 300)
            servers.append(f'        VpnServer("{id}", "{name}", "{code}", "{ip}", {latency})')

        out = '''package com.adblocker.vpn.data.model

data class VpnServer(
    val id: String,
    val name: String,
    val countryCode: String,
    val ipAddress: String,
    val latencyMs: Int
)

object VpnServerProvider {
    val getServers = listOf(
'''
        out += ',\n'.join(servers)
        out += '''
    )
}
'''
        with open('app/src/main/kotlin/com/adblocker/vpn/data/model/VpnServer.kt', 'w', encoding='utf-8') as f:
            f.write(out)
        print(f'Successfully wrote {len(servers)} servers to VpnServer.kt')
except Exception as e:
    print('Failed:', e)
