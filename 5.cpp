// 重命名标识符
#include <stdio.h>
#include <string.h>
using namespace std;
int n,new_sum,a[20];
bool b[100]={0},c[100]={0},d[100]={0};
char NEW_MAP[25][25];
void print()
{
    new_sum++;
    if(new_sum<=1)
    {

        for(int i=1;i<=n;i++){
            NEW_MAP[i][a[i]]='Q';
        }
    }
}
void queen(int i)
{
    for(int j=1;j<=n;j++)
    {
       if((b[j]==0)&&(c[i+j]==0)&&(d[i-j+n]==0))
       {
        a[i]=j;
        b[j]=1;
        c[i+j]=1;
        d[i-j+n]=1;
        if(i==n)
            print();
        else
            queen(i+1);
        b[j]=0;
        c[i+j]=0;
        d[i-j+n]=0;
        if(new_sum)
            break;
       }
    }
}
int main()
{
    while(~scanf("%d",&n)&&n){
        memset(NEW_MAP,'.',sizeof(NEW_MAP));
        memset(a,0,sizeof(a));
        memset(b,0,sizeof(b));
        memset(c,0,sizeof(c));
        memset(d,0,sizeof(d));
        new_sum=0;
        queen(1);
        if(new_sum){
            for(int i=1;i<=n;i++){
                for(int j=1;j<=n;j++)
                    printf("%c ",NEW_MAP[i][j]);
                printf("\n");
            }
        }
        else
            printf("No answer.\n");
        printf("\n");
    }
    return 0;
}
