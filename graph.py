# Round Robin Load Balancer Visualization for Google Colab
# Upload your load_balancer_data.csv file first!

import pandas as pd
import matplotlib.pyplot as plt
import seaborn as sns
import numpy as np
from google.colab import files

# Set style for better-looking plots
sns.set_style("whitegrid")
plt.rcParams['figure.figsize'] = (14, 10)

print("=" * 60)
print("Round Robin Load Balancer - Visualization Tool")
print("=" * 60)

# Upload CSV file
print("\n📤 Please upload your load_balancer_data.csv file:")
uploaded = files.upload()

if 'load_balancer_data.csv' not in uploaded:
    print("\n❌ ERROR: load_balancer_data.csv not uploaded!")
    print("Please run this cell again and select the correct file.")
else:
    print("\n✅ File uploaded successfully!")

def load_and_parse_data(filename):
    """Load the CSV data exported from Java simulation"""
    try:
        # Read all lines
        with open(filename, 'r') as f:
            lines = f.readlines()
        
        print(f"  Total lines read: {len(lines)}")
        
        # Find where load history starts
        split_idx = None
        for i, line in enumerate(lines):
            if line.startswith('# Server Load History'):
                split_idx = i
                break
        
        if split_idx is None:
            raise ValueError("Could not find '# Server Load History' marker in CSV")
        
        print(f"  Split index found at line: {split_idx}")
        
        # Parse metrics (include header line)
        metrics_data = ''.join(lines[0:split_idx])
        metrics_df = pd.read_csv(pd.io.common.StringIO(metrics_data))
        
        # Parse load history (skip blank line, include header)
        history_data = ''.join(lines[split_idx+1:])
        history_df = pd.read_csv(pd.io.common.StringIO(history_data))
        
        print(f"  Metrics columns: {list(metrics_df.columns)}")
        print(f"  History columns: {list(history_df.columns)}")
        
        return metrics_df, history_df
        
    except FileNotFoundError:
        print(f"\n❌ ERROR: File '{filename}' not found!")
        print("   Please upload the file first.")
        return None, None
    except Exception as e:
        print(f"\n❌ ERROR parsing file: {str(e)}")
        print("\nFirst 10 lines of the file:")
        with open(filename, 'r') as f:
            for i, line in enumerate(f):
                if i < 10:
                    print(f"  Line {i}: {line.rstrip()}")
        return None, None

def plot_request_distribution(metrics_df):
    """Plot 1: Request distribution across servers"""
    plt.figure(figsize=(10, 6))
    
    request_counts = metrics_df['server_id'].value_counts().sort_index()
    bars = plt.bar(request_counts.index, request_counts.values, 
                   color='steelblue', edgecolor='black', alpha=0.7)
    
    plt.xlabel('Server ID', fontsize=12, fontweight='bold')
    plt.ylabel('Number of Requests', fontsize=12, fontweight='bold')
    plt.title('Round Robin: Request Distribution Across Servers', 
              fontsize=14, fontweight='bold')
    plt.xticks(request_counts.index)
    plt.grid(axis='y', alpha=0.3)
    
    # Add value labels on bars
    for bar in bars:
        height = bar.get_height()
        plt.text(bar.get_x() + bar.get_width()/2., height,
                f'{int(height)}',
                ha='center', va='bottom', fontweight='bold')
    
    plt.tight_layout()
    plt.show()
    print("✓ Request Distribution Plot Created")

def plot_server_loads_over_time(history_df):
    """Plot 2: Server loads over time"""
    plt.figure(figsize=(12, 6))
    
    for server_id in sorted(history_df['server_id'].unique()):
        server_data = history_df[history_df['server_id'] == server_id]
        plt.plot(server_data['time_step'], server_data['load'], 
                marker='o', markersize=3, label=f'Server {server_id}', linewidth=2)
    
    plt.xlabel('Time Step', fontsize=12, fontweight='bold')
    plt.ylabel('Server Load', fontsize=12, fontweight='bold')
    plt.title('Server Loads Over Time - Round Robin Algorithm', 
              fontsize=14, fontweight='bold')
    plt.legend(loc='best', frameon=True, shadow=True)
    plt.grid(alpha=0.3)
    
    plt.tight_layout()
    plt.show()
    print("✓ Server Loads Timeline Created")

def plot_load_heatmap(history_df):
    """Plot 3: Heatmap of server loads"""
    plt.figure(figsize=(14, 6))
    
    # Pivot data for heatmap
    pivot_data = history_df.pivot(index='server_id', columns='time_step', values='load')
    pivot_data = pivot_data.fillna(0)
    
    sns.heatmap(pivot_data, cmap='YlOrRd', cbar_kws={'label': 'Load'}, 
                linewidths=0.5, linecolor='gray')
    
    plt.xlabel('Time Step', fontsize=12, fontweight='bold')
    plt.ylabel('Server ID', fontsize=12, fontweight='bold')
    plt.title('Server Load Heatmap - Round Robin Distribution', 
              fontsize=14, fontweight='bold')
    
    plt.tight_layout()
    plt.show()
    print("✓ Load Heatmap Created")

def plot_load_statistics(metrics_df):
    """Plot 4: Box plot of server load distribution"""
    plt.figure(figsize=(10, 6))
    
    # Group by server and get load statistics
    server_loads = [metrics_df[metrics_df['server_id'] == sid]['server_load'].values 
                   for sid in sorted(metrics_df['server_id'].unique())]
    
    box = plt.boxplot(server_loads, labels=[f'S{i}' for i in range(len(server_loads))],
                      patch_artist=True, showmeans=True)
    
    # Color the boxes
    for patch in box['boxes']:
        patch.set_facecolor('lightblue')
        patch.set_alpha(0.7)
    
    plt.xlabel('Server ID', fontsize=12, fontweight='bold')
    plt.ylabel('Load Distribution', fontsize=12, fontweight='bold')
    plt.title('Server Load Distribution Statistics', fontsize=14, fontweight='bold')
    plt.grid(axis='y', alpha=0.3)
    
    plt.tight_layout()
    plt.show()
    print("✓ Load Statistics Plot Created")

def create_summary_dashboard(metrics_df, history_df):
    """Create a comprehensive dashboard"""
    fig, axes = plt.subplots(2, 2, figsize=(16, 12))
    fig.suptitle('Round Robin Load Balancer - Performance Dashboard', 
                 fontsize=16, fontweight='bold')
    
    # Plot 1: Request distribution
    ax1 = axes[0, 0]
    request_counts = metrics_df['server_id'].value_counts().sort_index()
    bars = ax1.bar(request_counts.index, request_counts.values, 
                   color='steelblue', edgecolor='black', alpha=0.7)
    ax1.set_xlabel('Server ID', fontweight='bold')
    ax1.set_ylabel('Number of Requests', fontweight='bold')
    ax1.set_title('Request Distribution', fontweight='bold')
    ax1.grid(axis='y', alpha=0.3)
    
    # Plot 2: Average load per server
    ax2 = axes[0, 1]
    avg_loads = metrics_df.groupby('server_id')['server_load'].mean()
    ax2.bar(avg_loads.index, avg_loads.values, color='coral', 
            edgecolor='black', alpha=0.7)
    ax2.set_xlabel('Server ID', fontweight='bold')
    ax2.set_ylabel('Average Load', fontweight='bold')
    ax2.set_title('Average Load per Server', fontweight='bold')
    ax2.grid(axis='y', alpha=0.3)
    
    # Plot 3: Load timeline
    ax3 = axes[1, 0]
    for server_id in sorted(history_df['server_id'].unique()):
        server_data = history_df[history_df['server_id'] == server_id]
        ax3.plot(server_data['time_step'], server_data['load'], 
                label=f'S{server_id}', linewidth=2, marker='o', markersize=2)
    ax3.set_xlabel('Time Step', fontweight='bold')
    ax3.set_ylabel('Load', fontweight='bold')
    ax3.set_title('Server Loads Over Time', fontweight='bold')
    ax3.legend(loc='best', ncol=2, fontsize=8)
    ax3.grid(alpha=0.3)
    
    # Plot 4: Load variance
    ax4 = axes[1, 1]
    load_std = metrics_df.groupby('server_id')['server_load'].std()
    ax4.bar(load_std.index, load_std.values, color='mediumseagreen', 
            edgecolor='black', alpha=0.7)
    ax4.set_xlabel('Server ID', fontweight='bold')
    ax4.set_ylabel('Load Std Dev', fontweight='bold')
    ax4.set_title('Load Variability per Server', fontweight='bold')
    ax4.grid(axis='y', alpha=0.3)
    
    plt.tight_layout()
    plt.show()
    print("✓ Summary Dashboard Created")

def main():
    # Load data
    print("\n[1/6] Loading data from CSV...")
    metrics_df, history_df = load_and_parse_data('load_balancer_data.csv')
    
    if metrics_df is None or history_df is None:
        print("\n❌ Failed to load data. Please check the file and try again.")
        return
    
    print(f"  Loaded {len(metrics_df)} request metrics")
    print(f"  Loaded {len(history_df)} load history records")
    
    # Generate visualizations
    print("\n[2/6] Creating request distribution plot...")
    plot_request_distribution(metrics_df)
    
    print("\n[3/6] Creating server load timeline...")
    plot_server_loads_over_time(history_df)
    
    print("\n[4/6] Creating load heatmap...")
    plot_load_heatmap(history_df)
    
    print("\n[5/6] Creating load statistics plot...")
    plot_load_statistics(metrics_df)
    
    print("\n[6/6] Creating summary dashboard...")
    create_summary_dashboard(metrics_df, history_df)
    
    print("\n" + "=" * 60)
    print("All visualizations generated successfully!")
    print("=" * 60)
    print("\n📊 All graphs are displayed above.")
    print("💡 Right-click on any graph to save it to your computer.")

# Run the visualization
if __name__ == "__main__":
    main()