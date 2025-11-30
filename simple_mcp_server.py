#!/usr/bin/env python3
"""
Servidor MCP (Model Context Protocol) de Ejemplo
================================================

Este servidor implementa el protocolo MCP usando STDIO como transporte.
Proporciona herramientas para consultar información de empleados, políticas
de la empresa y saldos de tiempo libre.

Herramientas disponibles:
- get_employee_info: Obtiene información de un empleado
- get_time_off_balance: Consulta saldo de días de vacaciones y enfermedad
- get_company_policy: Obtiene políticas de la empresa

Protocolo: JSON-RPC 2.0 sobre STDIO
Autor: Geovanny Mendoza
Versión: 1.0.0
"""

import json
import sys
import logging
from typing import Dict, List, Any, Optional

# Configurar logging a stderr para no interferir con STDIO
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s',
    stream=sys.stderr
)
logger = logging.getLogger(__name__)

# ============================================================================
# BASE DE DATOS SIMULADA
# ============================================================================

EMPLOYEES_DB = {
    "EMP001": {
        "name": "Juan Pérez",
        "employee_id": "EMP001",
        "department": "Engineering",
        "position": "Senior Software Engineer",
        "hire_date": "2020-01-15",
        "email": "juan.perez@company.com",
        "manager": "Maria García",
        "vacation_days": 15,
        "sick_days": 10
    },
    "EMP002": {
        "name": "María García",
        "employee_id": "EMP002",
        "department": "Human Resources",
        "position": "HR Manager",
        "hire_date": "2018-03-20",
        "email": "maria.garcia@company.com",
        "manager": "Carlos López",
        "vacation_days": 20,
        "sick_days": 12
    },
    "EMP003": {
        "name": "Carlos López",
        "employee_id": "EMP003",
        "department": "Management",
        "position": "Director",
        "hire_date": "2015-06-10",
        "email": "carlos.lopez@company.com",
        "manager": "CEO",
        "vacation_days": 25,
        "sick_days": 15
    }
}

POLICIES_DB = {
    "vacation": {
        "name": "Política de Vacaciones",
        "description": "Los empleados tienen derecho a días de vacaciones pagadas según su antigüedad",
        "rules": [
            "0-2 años: 15 días por año",
            "3-5 años: 20 días por año",
            "5+ años: 25 días por año",
            "Las vacaciones deben solicitarse con 2 semanas de anticipación",
            "Máximo 10 días consecutivos sin aprobación especial"
        ]
    },
    "remote_work": {
        "name": "Política de Trabajo Remoto",
        "description": "Lineamientos para el trabajo desde casa",
        "rules": [
            "Hasta 3 días por semana de trabajo remoto",
            "Debe notificarse con 24 horas de anticipación",
            "Disponibilidad durante horario laboral obligatoria",
            "Reuniones presenciales tienen prioridad"
        ]
    },
    "sick_leave": {
        "name": "Política de Licencia por Enfermedad",
        "description": "Procedimientos para ausencias por enfermedad",
        "rules": [
            "Notificar antes de las 9:00 AM del día de ausencia",
            "Certificado médico requerido después de 3 días",
            "10-15 días pagados por año según antigüedad",
            "Días adicionales pueden ser no pagados"
        ]
    },
    "benefits": {
        "name": "Beneficios para Empleados",
        "description": "Paquete de beneficios de la empresa",
        "rules": [
            "Seguro médico para empleado y familia",
            "Plan de retiro con contribución del 5%",
            "Descuentos en gimnasios y educación",
            "Días personales: 3 por año",
            "Bono anual por desempeño"
        ]
    }
}

# ============================================================================
# IMPLEMENTACIÓN DE HERRAMIENTAS
# ============================================================================

def get_employee_info(employee_id: str) -> Dict[str, Any]:
    """
    Obtiene información detallada de un empleado.
    
    Args:
        employee_id: ID del empleado (ej: EMP001)
    
    Returns:
        Diccionario con información del empleado
    """
    logger.info(f"Getting employee info for: {employee_id}")
    
    if employee_id not in EMPLOYEES_DB:
        return {
            "error": "Employee not found",
            "employee_id": employee_id,
            "message": f"No se encontró información para el empleado {employee_id}"
        }
    
    employee = EMPLOYEES_DB[employee_id]
    return {
        "name": employee["name"],
        "employee_id": employee["employee_id"],
        "department": employee["department"],
        "position": employee["position"],
        "hire_date": employee["hire_date"],
        "email": employee["email"],
        "manager": employee["manager"]
    }


def get_time_off_balance(employee_id: str) -> Dict[str, Any]:
    """
    Consulta el saldo de días de vacaciones y enfermedad.
    
    Args:
        employee_id: ID del empleado
    
    Returns:
        Diccionario con saldos de tiempo libre
    """
    logger.info(f"Getting time off balance for: {employee_id}")
    
    if employee_id not in EMPLOYEES_DB:
        return {
            "error": "Employee not found",
            "employee_id": employee_id,
            "message": f"No se encontró información para el empleado {employee_id}"
        }
    
    employee = EMPLOYEES_DB[employee_id]
    return {
        "employee_id": employee_id,
        "name": employee["name"],
        "vacation_days": employee["vacation_days"],
        "sick_days": employee["sick_days"],
        "total_days": employee["vacation_days"] + employee["sick_days"]
    }


def get_company_policy(policy_name: str) -> Dict[str, Any]:
    """
    Obtiene información sobre una política de la empresa.
    
    Args:
        policy_name: Nombre de la política (vacation, remote_work, sick_leave, benefits)
    
    Returns:
        Diccionario con la información de la política
    """
    logger.info(f"Getting company policy: {policy_name}")
    
    policy_key = policy_name.lower().replace(" ", "_")
    
    if policy_key not in POLICIES_DB:
        return {
            "error": "Policy not found",
            "policy_name": policy_name,
            "available_policies": list(POLICIES_DB.keys()),
            "message": f"No se encontró la política '{policy_name}'"
        }
    
    policy = POLICIES_DB[policy_key]
    return {
        "policy_name": policy["name"],
        "description": policy["description"],
        "rules": policy["rules"]
    }


# ============================================================================
# DEFINICIONES DE HERRAMIENTAS (MCP Schema)
# ============================================================================

TOOLS = [
    {
        "name": "get_employee_info",
        "description": "Obtiene información detallada de un empleado incluyendo nombre, departamento, posición y más",
        "inputSchema": {
            "type": "object",
            "properties": {
                "employee_id": {
                    "type": "string",
                    "description": "ID del empleado (ej: EMP001)"
                }
            },
            "required": ["employee_id"]
        }
    },
    {
        "name": "get_time_off_balance",
        "description": "Consulta el saldo disponible de días de vacaciones y licencia por enfermedad de un empleado",
        "inputSchema": {
            "type": "object",
            "properties": {
                "employee_id": {
                    "type": "string",
                    "description": "ID del empleado"
                }
            },
            "required": ["employee_id"]
        }
    },
    {
        "name": "get_company_policy",
        "description": "Obtiene información sobre políticas de la empresa (vacation, remote_work, sick_leave, benefits)",
        "inputSchema": {
            "type": "object",
            "properties": {
                "policy_name": {
                    "type": "string",
                    "description": "Nombre de la política a consultar",
                    "enum": ["vacation", "remote_work", "sick_leave", "benefits"]
                }
            },
            "required": ["policy_name"]
        }
    }
]

# ============================================================================
# MANEJADOR DE PETICIONES JSON-RPC
# ============================================================================

def handle_tools_list() -> Dict[str, Any]:
    """Maneja la petición tools/list"""
    return {
        "tools": TOOLS
    }


def handle_tool_call(tool_name: str, arguments: Dict[str, Any]) -> Dict[str, Any]:
    """
    Ejecuta una herramienta y retorna el resultado.
    
    Args:
        tool_name: Nombre de la herramienta a ejecutar
        arguments: Argumentos para la herramienta
    
    Returns:
        Resultado de la ejecución
    """
    logger.info(f"Calling tool: {tool_name} with args: {arguments}")
    
    if tool_name == "get_employee_info":
        return get_employee_info(arguments.get("employee_id", ""))
    
    elif tool_name == "get_time_off_balance":
        return get_time_off_balance(arguments.get("employee_id", ""))
    
    elif tool_name == "get_company_policy":
        return get_company_policy(arguments.get("policy_name", ""))
    
    else:
        raise ValueError(f"Unknown tool: {tool_name}")


def create_response(request_id: Any, result: Any) -> Dict[str, Any]:
    """Crea una respuesta JSON-RPC exitosa"""
    return {
        "jsonrpc": "2.0",
        "id": request_id,
        "result": result
    }


def create_error_response(request_id: Any, code: int, message: str) -> Dict[str, Any]:
    """Crea una respuesta JSON-RPC de error"""
    return {
        "jsonrpc": "2.0",
        "id": request_id,
        "error": {
            "code": code,
            "message": message
        }
    }


def process_request(request: Dict[str, Any]) -> Dict[str, Any]:
    """
    Procesa una petición JSON-RPC.
    
    Args:
        request: Petición JSON-RPC
    
    Returns:
        Respuesta JSON-RPC
    """
    request_id = request.get("id")
    method = request.get("method")
    params = request.get("params", {})
    
    logger.info(f"Processing request - Method: {method}, ID: {request_id}")
    
    try:
        if method == "tools/list":
            result = handle_tools_list()
            return create_response(request_id, result)
        
        elif method == "tools/call":
            tool_name = params.get("name")
            arguments = params.get("arguments", {})
            
            if not tool_name:
                return create_error_response(
                    request_id,
                    -32602,
                    "Missing tool name"
                )
            
            result = handle_tool_call(tool_name, arguments)
            
            # Formato de respuesta MCP
            return create_response(request_id, {
                "content": [
                    {
                        "type": "text",
                        "text": json.dumps(result, indent=2)
                    }
                ]
            })
        
        else:
            return create_error_response(
                request_id,
                -32601,
                f"Method not found: {method}"
            )
    
    except Exception as e:
        logger.error(f"Error processing request: {e}", exc_info=True)
        return create_error_response(
            request_id,
            -32603,
            f"Internal error: {str(e)}"
        )


# ============================================================================
# MAIN LOOP
# ============================================================================

def main():
    """Loop principal del servidor MCP"""
    logger.info("MCP Server starting...")
    logger.info(f"Available tools: {[tool['name'] for tool in TOOLS]}")
    logger.info(f"Employees in database: {list(EMPLOYEES_DB.keys())}")
    logger.info("Waiting for requests on STDIN...")
    
    try:
        for line in sys.stdin:
            line = line.strip()
            if not line:
                continue
            
            try:
                # Parse JSON-RPC request
                request = json.loads(line)
                logger.debug(f"Received request: {request}")
                
                # Process request
                response = process_request(request)
                
                # Send response
                response_json = json.dumps(response)
                print(response_json)
                sys.stdout.flush()
                
                logger.debug(f"Sent response: {response}")
                
            except json.JSONDecodeError as e:
                logger.error(f"Invalid JSON: {e}")
                error_response = create_error_response(
                    None,
                    -32700,
                    "Parse error: Invalid JSON"
                )
                print(json.dumps(error_response))
                sys.stdout.flush()
    
    except KeyboardInterrupt:
        logger.info("Server stopped by user")
    except Exception as e:
        logger.error(f"Unexpected error: {e}", exc_info=True)
    finally:
        logger.info("MCP Server shutting down...")


if __name__ == "__main__":
    main()
